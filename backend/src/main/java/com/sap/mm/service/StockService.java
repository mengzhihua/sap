package com.sap.mm.service;

import com.sap.common.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sap.mm.entity.Stock;
import com.sap.mm.mapper.StockMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
@Service
public class StockService {
    private final StockMapper mapper;

    public StockService(StockMapper mapper) { this.mapper = mapper; }

    @Transactional
    public void change(String matnr, String werks, String lgort, BigDecimal qty, BigDecimal value) {
        if (mapper.adjust(matnr, werks, lgort, qty, value) > 0) {
            return;
        }

        Stock current = find(matnr, werks, lgort);
        if (current != null || qty.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException("库存不足: " + matnr);
        }

        Stock created = new Stock();
        created.setMatnr(matnr);
        created.setWerks(werks);
        created.setLgort(lgort);
        created.setUnrestrictedQty(qty);
        created.setStockValue(value);
        try {
            mapper.insert(created);
        } catch (DuplicateKeyException e) {
            if (mapper.adjust(matnr, werks, lgort, qty, value) == 0) {
                throw new BizException("库存不足: " + matnr);
            }
        }
    }

    private Stock find(String matnr, String werks, String lgort) {
        return mapper.selectOne(new LambdaQueryWrapper<Stock>()
                .eq(Stock::getMatnr, matnr)
                .eq(Stock::getWerks, werks)
                .eq(Stock::getLgort, lgort));
    }
}
