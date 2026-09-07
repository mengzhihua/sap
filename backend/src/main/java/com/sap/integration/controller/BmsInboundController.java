package com.sap.integration.controller;

import com.sap.common.R;
import com.sap.integration.dto.BmsStatementRequest;
import com.sap.integration.service.BmsStatementService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/open")
public class BmsInboundController {
    private final BmsStatementService service;
    private final String apiKey;

    public BmsInboundController(BmsStatementService service,
                                @Value("${sap.open.api-key:sap-open-key}") String apiKey) {
        this.service = service;
        this.apiKey = apiKey;
    }

    @PostMapping("/bms/statements")
    public R<Map<String, Object>> statement(@RequestHeader(value = "X-Api-Key", required = false) String supplied,
                                             @RequestBody BmsStatementRequest body) {
        check(supplied);
        com.sap.integration.entity.BmsStatement saved = service.post(body);
        return R.ok(map("belnr", saved.getBelnr(), "gjahr", String.valueOf(java.time.LocalDate.now().getYear()),
                "statementNo", saved.getStatementNo()));
    }

    @GetMapping("/bms/statements/{statementNo}")
    public R<Map<String, Object>> get(@RequestHeader(value = "X-Api-Key", required = false) String supplied,
                                      @PathVariable String statementNo) {
        check(supplied);
        com.sap.integration.entity.BmsStatement saved = service.find(statementNo);
        return R.ok(map("belnr", saved.getBelnr(), "gjahr", String.valueOf(java.time.LocalDate.now().getYear()),
                "statementNo", saved.getStatementNo()));
    }

    private void check(String supplied) {
        if (apiKey == null || apiKey.trim().isEmpty() || !apiKey.equals(supplied)) {
            throw new BmsAuthException("X-Api-Key 无效");
        }
    }

    @ExceptionHandler(BmsAuthException.class)
    public org.springframework.http.ResponseEntity<R<Void>> authError(BmsAuthException e) {
        return org.springframework.http.ResponseEntity.status(401).body(R.fail(401, e.getMessage()));
    }

    private static Map<String, Object> map(String k1, Object v1, String k2, Object v2, String k3, Object v3) {
        java.util.Map<String, Object> m = new java.util.HashMap<>();
        m.put(k1, v1);
        m.put(k2, v2);
        m.put(k3, v3);
        return m;
    }

    private static class BmsAuthException extends RuntimeException {
        private BmsAuthException(String message) {
            super(message);
        }
    }
}
