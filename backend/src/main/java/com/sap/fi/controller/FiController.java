package com.sap.fi.controller;
import com.sap.common.R;
import com.sap.fi.dto.*;
import com.sap.fi.service.AccountingDocumentService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.*;

@RestController @RequestMapping("/api/fi")
public class FiController {
    private final JdbcTemplate jdbc;private final AccountingDocumentService accounting;
    public FiController(JdbcTemplate jdbc,AccountingDocumentService accounting){this.jdbc=jdbc;this.accounting=accounting;}
    @GetMapping("/gl-accounts") public R<List<Map<String,Object>>> accounts(){return R.ok(jdbc.queryForList("SELECT * FROM sap_gl_account"));}
    @PostMapping("/documents") public R<Map<String,Object>> post(@Valid @RequestBody FiDocumentRequest request){return R.ok(accounting.post(request));}
    @GetMapping("/documents") public R<List<Map<String,Object>>> docs(@RequestParam(required=false)String blart){return R.ok(blart==null?jdbc.queryForList("SELECT * FROM sap_acc_document"):jdbc.queryForList("SELECT * FROM sap_acc_document WHERE blart=? ORDER BY id DESC",blart));}
    @GetMapping("/documents/{id}") public R<Map<String,Object>> doc(@PathVariable String id){Map<String,Object> result=jdbc.queryForMap("SELECT * FROM sap_acc_document WHERE id=? OR belnr=?",id,id);result.put("items",jdbc.queryForList("SELECT * FROM sap_acc_document_item WHERE belnr=?",result.get("belnr")));return R.ok(result);}
    @PostMapping("/documents/{id}/reverse") public R<Map<String,Object>> reverse(@PathVariable String id){return R.ok(accounting.reverse(id));}
    @PostMapping("/payments") public R<Map<String,Object>> payment(@Valid @RequestBody PaymentRequest request){return R.ok(accounting.payment(request));}
    @GetMapping("/balances") public R<List<Map<String,Object>>> balances(){return R.ok(jdbc.queryForList("SELECT saknr,SUM(CASE WHEN shkzg='S' THEN amount ELSE -amount END) balance FROM sap_acc_document_item GROUP BY saknr"));}
    @GetMapping("/ap/open-items") public R<List<Map<String,Object>>> ap(@RequestParam(required=false)String lifnr){return R.ok(lifnr==null?jdbc.queryForList("SELECT * FROM sap_acc_document_item WHERE saknr='2201'"):jdbc.queryForList("SELECT * FROM sap_acc_document_item WHERE saknr='2201' AND lifnr=?",lifnr));}
    @GetMapping("/ar/open-items") public R<List<Map<String,Object>>> ar(@RequestParam(required=false)String kunnr){return R.ok(kunnr==null?jdbc.queryForList("SELECT * FROM sap_acc_document_item WHERE saknr='1122'"):jdbc.queryForList("SELECT * FROM sap_acc_document_item WHERE saknr='1122' AND kunnr=?",kunnr));}
    @GetMapping("/account-determination") public R<List<Map<String,Object>>> determination(){return R.ok(jdbc.queryForList("SELECT * FROM sap_account_determination"));}
}
