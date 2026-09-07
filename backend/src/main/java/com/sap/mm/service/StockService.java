package com.sap.mm.service;

import com.sap.common.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sap.mm.entity.Stock;
import com.sap.mm.mapper.StockMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
@Service
public class StockService {
    private final StockMapper mapper;
    public StockService(StockMapper mapper) { this.mapper = mapper; }

    @Transactional
    public void change(String matnr, String werks, String lgort, BigDecimal qty, BigDecimal value) {
        Stock current = mapper.selectOne(new LambdaQueryWrapper<Stock>()
                .eq(Stock::getMatnr, matnr).eq(Stock::getWerks, werks).eq(Stock::getLgort, lgort));
        if (current == null) {
            if (qty.compareTo(BigDecimal.ZERO) < 0) throw new BizException("库存不足: " + matnr);
            current = new Stock();
            current.setMatnr(matnr); current.setWerks(werks); current.setLgort(lgort);
            current.setUnrestrictedQty(qty); current.setStockValue(value);
            mapper.insert(current);
            return;
        }
        BigDecimal quantity = current.getUnrestrictedQty() == null ? BigDecimal.ZERO : current.getUnrestrictedQty();
        if (quantity.add(qty).compareTo(BigDecimal.ZERO) < 0) throw new BizException("库存不足: " + matnr);
        current.setUnrestrictedQty(quantity.add(qty));
        current.setStockValue((current.getStockValue() == null ? BigDecimal.ZERO : current.getStockValue()).add(value));
        mapper.update(current, new LambdaQueryWrapper<Stock>()
                .eq(Stock::getMatnr, matnr).eq(Stock::getWerks, werks).eq(Stock::getLgort, lgort));
    }
}
