package com.sap.integration.controller;

import com.sap.integration.dto.*;
import com.sap.integration.service.IntegrationLogService;
import com.sap.integration.service.SrmInboundService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class SrmInboundController {
    private final SrmInboundService service;
    private final IntegrationLogService logs;
    private final String username;
    private final String password;

    public SrmInboundController(SrmInboundService service, IntegrationLogService logs,
                                @Value("${sap.srm.username:srm}") String username,
                                @Value("${sap.srm.password:srm123}") String password) {
        this.service = service;
        this.logs = logs;
        this.username = username;
        this.password = password;
    }

    @PostMapping("/API_PURCHASEORDER_PROCESS_SRV/A_PurchaseOrder")
    public ResponseEntity<?> purchaseOrder(@RequestHeader(value = "Authorization", required = false) String auth,
                                           @RequestBody SrmPurchaseOrderPayload body) {
        check(auth);
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> po = service.po(body);
            Map<String, Object> d = new HashMap<>();
            d.put("PurchaseOrder", po.get("ebeln"));
            log("SRM_CREATE_PO", body, d, true, null, start);
            return ResponseEntity.ok(map("d", d));
        } catch (RuntimeException e) {
            log("SRM_CREATE_PO", body, null, false, e.getMessage(), start);
            return error(e);
        }
    }

    @PostMapping("/API_MATERIAL_DOCUMENT_SRV/A_MaterialDocumentHeader")
    public ResponseEntity<?> material(@RequestHeader(value = "Authorization", required = false) String auth,
                                      @RequestBody SrmGoodsReceiptPayload body) {
        check(auth);
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> doc = service.gr(body);
            Map<String, Object> d = new HashMap<>();
            d.put("MaterialDocument", doc.get("mblnr"));
            d.put("MaterialDocumentYear", doc.get("mjahr"));
            log("SRM_POST_GR", body, d, true, null, start);
            return ResponseEntity.ok(map("d", d));
        } catch (RuntimeException e) {
            log("SRM_POST_GR", body, null, false, e.getMessage(), start);
            return error(e);
        }
    }

    @PostMapping("/API_SUPPLIERINVOICE_PROCESS_SRV/A_SupplierInvoice")
    public ResponseEntity<?> invoice(@RequestHeader(value = "Authorization", required = false) String auth,
                                     @RequestBody SrmInvoicePayload body) {
        check(auth);
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> inv = service.invoice(body);
            Map<String, Object> d = new HashMap<>();
            d.put("SupplierInvoice", inv.get("belnr"));
            d.put("FiscalYear", inv.get("gjahr"));
            log("SRM_POST_INVOICE", body, d, true, null, start);
            return ResponseEntity.ok(map("d", d));
        } catch (RuntimeException e) {
            log("SRM_POST_INVOICE", body, null, false, e.getMessage(), start);
            return error(e);
        }
    }

    @PostMapping("/API_SUPPLIER_EVALUATION_SRV/A_SupplierEvaluation")
    public ResponseEntity<?> evaluation(@RequestHeader(value = "Authorization", required = false) String auth,
                                        @RequestBody SrmEvaluationPayload body) {
        check(auth);
        long start = System.currentTimeMillis();
        try {
            String evaluationId = service.evaluation(body);
            Map<String, Object> result = new HashMap<>();
            result.put("EvaluationId", evaluationId);
            logs.write("IN", "SRM", "SRM_SYNC_EVAL", body, result, true, null, System.currentTimeMillis() - start);
            return ResponseEntity.ok(map("d", result));
        } catch (RuntimeException e) {
            log("SRM_SYNC_EVAL", body, null, false, e.getMessage(), start);
            return error(e);
        }
    }

    private void check(String header) {
        if (header == null || !header.startsWith("Basic ")) throw new SrmAuthException("SRM Basic Auth required");
        try {
            String raw = new String(java.util.Base64.getDecoder().decode(header.substring(6)), java.nio.charset.StandardCharsets.UTF_8);
            int colon = raw.indexOf(':');
            if (colon < 0 || !username.equals(raw.substring(0, colon)) || !password.equals(raw.substring(colon + 1))) {
                throw new SrmAuthException("SRM Basic Auth invalid");
            }
        } catch (IllegalArgumentException e) {
            throw new SrmAuthException("SRM Basic Auth invalid");
        }
    }

    @ExceptionHandler(SrmAuthException.class)
    public ResponseEntity<Map<String, Object>> authError(SrmAuthException e) {
        Map<String, Object> message = new HashMap<>();
        message.put("lang", "zh");
        message.put("value", e.getMessage());
        Map<String, Object> error = new HashMap<>();
        error.put("code", "SAP_AUTH_ERROR");
        error.put("message", message);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(map("error", error));
    }

    private ResponseEntity<Map<String, Object>> error(RuntimeException e) {
        Map<String, Object> message = new HashMap<>();
        message.put("lang", "zh");
        message.put("value", e.getMessage());
        Map<String, Object> error = new HashMap<>();
        error.put("code", "SAP_ERROR");
        error.put("message", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map("error", error));
    }

    private void log(String action, Object request, Object response, boolean success, String error, long start) {
        logs.write("IN", "SRM", action, request, response, success, error, System.currentTimeMillis() - start);
    }

    private static Map<String, Object> map(String key, Object value) {
        Map<String, Object> m = new HashMap<>();
        m.put(key, value);
        return m;
    }

    private static class SrmAuthException extends RuntimeException {
        private SrmAuthException(String message) {
            super(message);
        }
    }

}
