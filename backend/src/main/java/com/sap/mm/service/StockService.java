package com.sap.mm.service;

import com.sap.common.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sap.mm.entity.Stock;
import com.sap.mm.mapper.StockMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class StockService {
    private final StockMapper mapper;
    private final TransactionTemplate requiresNew;

    public StockService(StockMapper mapper, PlatformTransactionManager transactionManager) {
        this.mapper = mapper;
        this.requiresNew = new TransactionTemplate(transactionManager);
        this.requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Transactional
    public void change(String matnr, String werks, String lgort, BigDecimal qty, BigDecimal value) {
        if (find(matnr, werks, lgort) == null) {
            ensureRow(matnr, werks, lgort);
        }

        if (mapper.adjust(matnr, werks, lgort, qty, value) == 0) {
            throw new BizException("库存不足: " + matnr);
        }
    }

    private void ensureRow(String matnr, String werks, String lgort) {
        requiresNew.execute(status -> {
            Stock created = new Stock();
            created.setMatnr(matnr);
            created.setWerks(werks);
            created.setLgort(lgort);
            created.setUnrestrictedQty(BigDecimal.ZERO);
            created.setStockValue(BigDecimal.ZERO);
            try {
                mapper.insert(created);
            } catch (DuplicateKeyException ignored) {
            }
            return null;
        });
    }

    private Stock find(String matnr, String werks, String lgort) {
        return mapper.selectOne(new LambdaQueryWrapper<Stock>()
                .eq(Stock::getMatnr, matnr)
                .eq(Stock::getWerks, werks)
                .eq(Stock::getLgort, lgort));
    }
}
