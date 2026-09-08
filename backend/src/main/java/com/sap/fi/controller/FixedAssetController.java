package com.sap.fi.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sap.common.R;
import com.sap.fi.dto.AssetAcquisitionRequest;
import com.sap.fi.dto.DepreciationRunRequest;
import com.sap.fi.dto.FixedAssetRequest;
import com.sap.fi.entity.AssetTransaction;
import com.sap.fi.entity.FixedAsset;
import com.sap.fi.mapper.AssetTransactionMapper;
import com.sap.fi.mapper.FixedAssetMapper;
import com.sap.fi.service.FixedAssetService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fi")
public class FixedAssetController {
    private final FixedAssetMapper assets;
    private final AssetTransactionMapper transactions;
    private final FixedAssetService service;

    public FixedAssetController(FixedAssetMapper assets, AssetTransactionMapper transactions, FixedAssetService service) {
        this.assets = assets;
        this.transactions = transactions;
        this.service = service;
    }

    @GetMapping("/assets")
    public R<List<FixedAsset>> assets(@RequestParam(required = false) String q) {
        LambdaQueryWrapper<FixedAsset> query = new LambdaQueryWrapper<>();
        if (q != null && !q.trim().isEmpty()) query.like(FixedAsset::getAnln1, q).or().like(FixedAsset::getName, q);
        query.orderByAsc(FixedAsset::getAnln1);
        return R.ok(assets.selectList(query));
    }

    @PostMapping("/assets")
    public R<FixedAsset> create(@Valid @RequestBody FixedAssetRequest request) { return R.ok(service.create(request)); }

    @PostMapping("/assets/{anln1}/acquisitions")
    public R<AssetTransaction> acquire(@PathVariable String anln1, @Valid @RequestBody AssetAcquisitionRequest request) {
        return R.ok(service.acquire(anln1, request));
    }

    @GetMapping("/assets/{anln1}/transactions")
    public R<List<AssetTransaction>> transactions(@PathVariable String anln1) {
        service.requireAsset(anln1);
        return R.ok(transactions.selectList(new LambdaQueryWrapper<AssetTransaction>()
                .eq(AssetTransaction::getAnln1, anln1).orderByDesc(AssetTransaction::getPostingDate, AssetTransaction::getId)));
    }

    @PostMapping("/depreciation-runs")
    public R<Map<String, Object>> depreciate(@Valid @RequestBody DepreciationRunRequest request) {
        return R.ok(service.depreciate(request));
    }
}
