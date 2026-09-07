package com.sap.common;

import com.sap.TestSupport;
import com.sap.basis.mapper.NumberRangeMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NumberRangeConcurrencyTest extends TestSupport {
    @Autowired private NumberRangeService numbers;
    @Autowired private NumberRangeMapper ranges;

    @Test
    void allocatesUniqueNumbersUnderConcurrency() throws Exception {
        final String object = "C-" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        ExecutorService executor = Executors.newFixedThreadPool(8);
        List<Future<String>> futures = new ArrayList<>();
        try {
            ranges.deleteById(object);
            for (int i = 0; i < 400; i++) {
                futures.add(executor.submit(new Callable<String>() {
                    @Override
                    public String call() {
                        return numbers.next(object);
                    }
                }));
            }

            Set<String> values = new HashSet<>();
            for (Future<String> future : futures) {
                values.add(future.get());
            }
            assertEquals(400, values.size());
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));
            ranges.deleteById(object);
        }
    }

    @Test
    void allocatesFirstNumbersForBrandNewObjectUnderConcurrency() throws Exception {
        final String object = "N-" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        ExecutorService executor = Executors.newFixedThreadPool(8);
        List<Future<String>> futures = new ArrayList<>();
        try {
            ranges.deleteById(object);
            for (int i = 0; i < 8; i++) {
                futures.add(executor.submit(new Callable<String>() {
                    @Override
                    public String call() {
                        return numbers.next(object, "N");
                    }
                }));
            }

            Set<String> values = new HashSet<>();
            for (Future<String> future : futures) {
                values.add(future.get());
            }
            assertEquals(8, values.size());
            for (int i = 1; i <= 8; i++) {
                assertTrue(values.contains("N" + String.format("%08d", i)));
            }
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));
            ranges.deleteById(object);
        }
    }
}
