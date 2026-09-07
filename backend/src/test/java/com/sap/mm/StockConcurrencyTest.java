package com.sap.mm;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sap.TestSupport;
import com.sap.common.BizException;
import com.sap.mm.entity.Stock;
import com.sap.mm.mapper.StockMapper;
import com.sap.mm.service.StockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class StockConcurrencyTest extends TestSupport {
    @Autowired private StockService stock;
    @Autowired private StockMapper stocks;

    @Test
    void conditionalStockUpdatesPreventOverselling() throws Exception {
        final String matnr = "CONC-" + UUID.randomUUID().toString().replace("-", "");
        final String werks = "1000";
        final String lgort = "9999";
        seed(matnr, werks, lgort, new BigDecimal("100"));

        ExecutorService executor = Executors.newFixedThreadPool(10);
        try {
            List<Future<Boolean>> deductions = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                deductions.add(executor.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        try {
                            stock.change(matnr, werks, lgort, new BigDecimal("-20"), BigDecimal.ZERO);
                            return true;
                        } catch (BizException e) {
                            return false;
                        }
                    }
                }));
            }

            int successes = 0;
            for (Future<Boolean> deduction : deductions) {
                successes += get(deduction) ? 1 : 0;
            }
            assertEquals(5, successes);
            assertEquals(0, stocks.selectOne(wrapper(matnr, werks, lgort)).getUnrestrictedQty().compareTo(BigDecimal.ZERO));

            List<Future<Boolean>> additions = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                additions.add(executor.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        stock.change(matnr, werks, lgort, BigDecimal.ONE, BigDecimal.ZERO);
                        return true;
                    }
                }));
            }
            for (Future<Boolean> addition : additions) {
                assertEquals(Boolean.TRUE, get(addition));
            }
            assertEquals(0, stocks.selectOne(wrapper(matnr, werks, lgort)).getUnrestrictedQty()
                    .compareTo(new BigDecimal("50")));
        } finally {
            executor.shutdownNow();
            stocks.delete(wrapper(matnr, werks, lgort));
        }
    }

    private void seed(String matnr, String werks, String lgort, BigDecimal quantity) {
        stocks.delete(wrapper(matnr, werks, lgort));
        Stock value = new Stock();
        value.setMatnr(matnr);
        value.setWerks(werks);
        value.setLgort(lgort);
        value.setUnrestrictedQty(quantity);
        value.setStockValue(BigDecimal.ZERO);
        stocks.insert(value);
    }

    private LambdaQueryWrapper<Stock> wrapper(String matnr, String werks, String lgort) {
        return new LambdaQueryWrapper<Stock>()
                .eq(Stock::getMatnr, matnr)
                .eq(Stock::getWerks, werks)
                .eq(Stock::getLgort, lgort);
    }

    private boolean get(Future<Boolean> future) throws Exception {
        try {
            return future.get();
        } catch (ExecutionException e) {
            fail("concurrent stock update failed", e.getCause());
            return false;
        }
    }
}
