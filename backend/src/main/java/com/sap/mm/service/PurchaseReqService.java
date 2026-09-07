package com.sap.mm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sap.common.BizException;
import com.sap.common.NumberRangeService;
import com.sap.mm.dto.PurchaseReqRequest;
import com.sap.mm.entity.PurchaseReq;
import com.sap.mm.entity.PurchaseReqItem;
import com.sap.mm.mapper.PurchaseReqItemMapper;
import com.sap.mm.mapper.PurchaseReqMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PurchaseReqService {
    private final PurchaseReqMapper requests;
    private final PurchaseReqItemMapper items;
    private final NumberRangeService numbers;
    private final ReferenceDataService refs;

    public PurchaseReqService(PurchaseReqMapper requests, PurchaseReqItemMapper items,
                              NumberRangeService numbers, ReferenceDataService refs) {
        this.requests = requests;
        this.items = items;
        this.numbers = numbers;
        this.refs = refs;
    }

    @Transactional
    public PurchaseReq create(PurchaseReqRequest request) {
        PurchaseReq saved = new PurchaseReq();
        saved.setBanfn(numbers.next("PR"));
        saved.setStatus("CREATED");
        saved.setRequester(request.getRequester());
        saved.setBukrs(request.getBukrs() == null ? "1000" : request.getBukrs());
        saved.setWerks(request.getWerks() == null ? "1000" : request.getWerks());
        saved.setTotalAmount(BigDecimal.ZERO);
        requests.insert(saved);
        BigDecimal total = BigDecimal.ZERO;
        int pos = 10;
        for (PurchaseReqRequest.Item input : request.getItems()) {
            PurchaseReqItem item = new PurchaseReqItem();
            item.setBanfn(saved.getBanfn());
            item.setBnfpo(String.valueOf(pos));
            item.setMatnr(refs.material(input.getMatnr()));
            item.setMenge(input.getMenge());
            item.setNetpr(input.getNetpr() == null ? BigDecimal.ZERO : input.getNetpr());
            item.setWerks(input.getWerks() == null ? saved.getWerks() : input.getWerks());
            item.setLgort(input.getLgort() == null ? "0001" : input.getLgort());
            if (item.getMenge() == null || item.getMenge().signum() <= 0) {
                throw new BizException("采购申请数量必须大于0");
            }
            items.insert(item);
            total = total.add(item.getMenge().multiply(item.getNetpr()));
            pos += 10;
        }
        saved.setTotalAmount(total);
        requests.updateById(saved);
        return one(saved.getBanfn());
    }

    public List<PurchaseReq> list() {
        List<PurchaseReq> result = requests.selectList(null);
        result.forEach(this::load);
        return result;
    }

    public PurchaseReq one(String banfn) {
        PurchaseReq result = requests.selectOne(new LambdaQueryWrapper<PurchaseReq>()
                .eq(PurchaseReq::getBanfn, banfn));
        if (result == null) throw new BizException("采购申请不存在: " + banfn);
        return load(result);
    }

    @Transactional
    public PurchaseReq release(String banfn) {
        PurchaseReq result = one(banfn);
        if ("ORDERED".equals(result.getStatus())) throw new BizException("采购申请已转采购订单");
        result.setStatus("RELEASED");
        requests.updateById(result);
        return load(result);
    }

    public void markOrdered(String banfn) {
        PurchaseReq result = one(banfn);
        result.setStatus("ORDERED");
        requests.updateById(result);
    }

    private PurchaseReq load(PurchaseReq request) {
        request.setItems(items.selectList(new LambdaQueryWrapper<PurchaseReqItem>()
                .eq(PurchaseReqItem::getBanfn, request.getBanfn())));
        return request;
    }
}
