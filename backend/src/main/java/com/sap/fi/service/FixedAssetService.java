package com.sap.fi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sap.common.BizException;
import com.sap.common.NumberRangeService;
import com.sap.fi.dto.AssetAcquisitionRequest;
import com.sap.fi.dto.DepreciationRunRequest;
import com.sap.fi.dto.FixedAssetRequest;
import com.sap.fi.entity.AssetTransaction;
import com.sap.fi.entity.FixedAsset;
import com.sap.fi.mapper.AssetTransactionMapper;
import com.sap.fi.mapper.FixedAssetMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class FixedAssetService {
    private final FixedAssetMapper assets;
    private final AssetTransactionMapper transactions;
    private final AccountingDocumentService accounting;
    private final NumberRangeService numbers;

    public FixedAssetService(FixedAssetMapper assets, AssetTransactionMapper transactions,
                             AccountingDocumentService accounting, NumberRangeService numbers) {
        this.assets = assets;
        this.transactions = transactions;
        this.accounting = accounting;
        this.numbers = numbers;
    }

    @Transactional
    public FixedAsset create(FixedAssetRequest request) {
        FixedAsset asset = new FixedAsset();
        asset.setAnln1(numbers.next("ASSET"));
        asset.setName(request.getName());
        asset.setAssetClass(request.getAssetClass());
        asset.setBukrs(request.getBukrs());
        asset.setKostl(request.getKostl());
        asset.setUsefulLifeMonths(request.getUsefulLifeMonths());
        asset.setSalvageValue(request.getSalvageValue());
        asset.setAcquisitionValue(BigDecimal.ZERO);
        asset.setAccumulatedDepreciation(BigDecimal.ZERO);
        asset.setBookValue(BigDecimal.ZERO);
        asset.setStatus("CREATED");
        assets.insert(asset);
        return asset;
    }

    @Transactional
    public AssetTransaction acquire(String anln1, AssetAcquisitionRequest request) {
        FixedAsset asset = requireAsset(anln1);
        LocalDate postingDate = request.getPostingDate() == null ? LocalDate.now() : request.getPostingDate();
        BigDecimal newAcquisition = value(asset.getAcquisitionValue()).add(request.getAmount());
        if (asset.getSalvageValue().compareTo(newAcquisition) > 0) {
            throw new BizException("残值不能高于资产原值");
        }
        String offset = blank(request.getOffsetAccount()) ? "1002" : request.getOffsetAccount();
        String belnr = accounting.post("AA", "FI-AA", anln1, Arrays.asList(
                new AccountingDocumentService.FiLine("1601", "S", request.getAmount(), asset.getKostl(), null, null, asset.getName()),
                new AccountingDocumentService.FiLine(offset, "H", request.getAmount(), null, null, null, "资产购置")
        ));
        asset.setAcquisitionValue(newAcquisition);
        asset.setBookValue(value(asset.getBookValue()).add(request.getAmount()));
        if (asset.getCapitalizationDate() == null || postingDate.isBefore(asset.getCapitalizationDate())) {
            asset.setCapitalizationDate(postingDate);
        }
        asset.setStatus("ACTIVE");
        assets.updateById(asset);
        return record(asset, "ACQUISITION", null, postingDate, request.getAmount(), belnr, request.getText());
    }

    @Transactional
    public Map<String, Object> depreciate(DepreciationRunRequest request) {
        YearMonth period = YearMonth.parse(request.getPeriod());
        LocalDate postingDate = request.getPostingDate() == null ? period.atEndOfMonth() : request.getPostingDate();
        if (!YearMonth.from(postingDate).equals(period)) throw new BizException("过账日期必须位于折旧期间内");
        int posted = 0;
        BigDecimal total = BigDecimal.ZERO;
        List<String> documents = new ArrayList<>();
        for (FixedAsset asset : assets.selectList(new LambdaQueryWrapper<FixedAsset>().eq(FixedAsset::getStatus, "ACTIVE"))) {
            if (asset.getCapitalizationDate() == null || period.isBefore(YearMonth.from(asset.getCapitalizationDate()))) continue;
            Long exists = transactions.selectCount(new LambdaQueryWrapper<AssetTransaction>()
                    .eq(AssetTransaction::getAnln1, asset.getAnln1())
                    .eq(AssetTransaction::getTransactionType, "DEPRECIATION")
                    .eq(AssetTransaction::getFiscalPeriod, request.getPeriod()));
            if (exists > 0) continue;
            BigDecimal depreciable = value(asset.getAcquisitionValue()).subtract(value(asset.getSalvageValue()));
            BigDecimal remaining = depreciable.subtract(value(asset.getAccumulatedDepreciation()));
            if (remaining.signum() <= 0) continue;
            BigDecimal amount = depreciable.divide(BigDecimal.valueOf(asset.getUsefulLifeMonths()), 2, RoundingMode.HALF_UP).min(remaining);
            if (amount.signum() <= 0) continue;
            String belnr = accounting.post("AF", "FI-AA", asset.getAnln1() + "/" + request.getPeriod(), Arrays.asList(
                    new AccountingDocumentService.FiLine("6602", "S", amount, asset.getKostl(), null, null, "折旧 " + request.getPeriod()),
                    new AccountingDocumentService.FiLine("1602", "H", amount, null, null, null, "累计折旧")
            ));
            asset.setAccumulatedDepreciation(value(asset.getAccumulatedDepreciation()).add(amount));
            asset.setBookValue(value(asset.getAcquisitionValue()).subtract(asset.getAccumulatedDepreciation()));
            assets.updateById(asset);
            record(asset, "DEPRECIATION", request.getPeriod(), postingDate, amount, belnr, "月度折旧");
            posted++;
            total = total.add(amount);
            documents.add(belnr);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("period", request.getPeriod());
        result.put("postedAssets", posted);
        result.put("amount", total);
        result.put("documents", documents);
        return result;
    }

    public FixedAsset requireAsset(String anln1) {
        FixedAsset asset = assets.selectById(anln1);
        if (asset == null) throw new BizException("固定资产不存在: " + anln1);
        return asset;
    }

    private AssetTransaction record(FixedAsset asset, String type, String period, LocalDate date,
                                    BigDecimal amount, String belnr, String text) {
        AssetTransaction transaction = new AssetTransaction();
        transaction.setAnln1(asset.getAnln1());
        transaction.setTransactionType(type);
        transaction.setFiscalPeriod(period);
        transaction.setPostingDate(date);
        transaction.setAmount(amount);
        transaction.setBelnr(belnr);
        transaction.setText(text);
        transactions.insert(transaction);
        return transaction;
    }

    private static BigDecimal value(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    private static boolean blank(String value) { return value == null || value.trim().isEmpty(); }
}
