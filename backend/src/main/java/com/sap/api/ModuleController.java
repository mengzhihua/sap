package com.sap.api;

import com.sap.common.R;
import com.sap.integration.service.IntegrationLogService;
import com.sap.integration.service.SapBusinessService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class ModuleController {
    private final SapBusinessService service;
    private final JdbcTemplate jdbc;
    private final IntegrationLogService logs;

    public ModuleController(SapBusinessService service, JdbcTemplate jdbc, IntegrationLogService logs) {
        this.service = service;
        this.jdbc = jdbc;
        this.logs = logs;
    }

    @GetMapping("/basis/users")
    public R<List<Map<String, Object>>> users() {
        return R.ok(jdbc.queryForList("SELECT id,username,real_name,role,status,last_login_at FROM sap_user"));
    }

    @GetMapping("/basis/roles")
    public R<List<Map<String, Object>>> roles() {
        return R.ok(Arrays.asList(map("code", "ADMIN", "name", "管理员"), map("code", "MM", "name", "物料管理"),
                map("code", "SD", "name", "销售管理"), map("code", "FI", "name", "财务管理"),
                map("code", "CO", "name", "成本管理"), map("code", "PP", "name", "生产管理")));
    }

    @GetMapping("/basis/org/{kind}")
    public R<List<Map<String, Object>>> org(@PathVariable String kind) {
        return R.ok(service.list(orgTable(kind)));
    }

    @PostMapping("/basis/org/{kind}")
    public R<Map<String, Object>> saveOrg(@PathVariable String kind, @RequestBody Map<String, Object> body) {
        String table = orgTable(kind);
        String key = "sap_company_code".equals(table) ? "bukrs" : "sap_plant".equals(table) ? "werks"
                : "sap_storage_location".equals(table) ? "lgort" : "sap_purchasing_org".equals(table) ? "ekorg"
                : "sap_purchasing_group".equals(table) ? "ekgrp" : "vkorg";
        return R.ok(service.saveMaster(table, key, body));
    }

    @GetMapping("/basis/tcodes")
    public R<List<Map<String, Object>>> tcodes(@RequestParam(required = false) String q) {
        if (q == null) return R.ok(service.list("sap_tcode"));
        return R.ok(jdbc.queryForList("SELECT * FROM sap_tcode WHERE tcode LIKE ? OR name LIKE ?", "%" + q + "%", "%" + q + "%"));
    }

    @GetMapping("/basis/op-logs")
    public R<List<Map<String, Object>>> opLogs() {
        return R.ok(service.list("sap_op_log"));
    }

    @GetMapping("/mm/materials")
    public R<List<Map<String, Object>>> materials(@RequestParam(required = false) String q) {
        return R.ok(service.listMaterials(q));
    }

    @PostMapping("/mm/materials")
    public R<Map<String, Object>> material(@RequestBody Map<String, Object> body) {
        return R.ok(service.saveMaster("sap_material", "matnr", body));
    }

    @GetMapping("/mm/vendors")
    public R<List<Map<String, Object>>> vendors(@RequestParam(required = false) String q) {
        return R.ok(service.listVendors(q));
    }

    @PostMapping("/mm/vendors")
    public R<Map<String, Object>> vendor(@RequestBody Map<String, Object> body) {
        return R.ok(service.saveMaster("sap_vendor", "lifnr", body));
    }

    @PostMapping("/mm/pr")
    public R<Map<String, Object>> pr(@RequestBody Map<String, Object> body) {
        return R.ok(service.createPr(body));
    }

    @GetMapping("/mm/pr")
    public R<List<Map<String, Object>>> prs() {
        return R.ok(service.list("sap_purchase_req"));
    }

    @PostMapping("/mm/pr/{id}/release")
    public R<Map<String, Object>> releasePr(@PathVariable String id) {
        jdbc.update("UPDATE sap_purchase_req SET status='RELEASED' WHERE banfn=?", id);
        return R.ok(jdbc.queryForMap("SELECT * FROM sap_purchase_req WHERE banfn=?", id));
    }

    @PostMapping("/mm/po")
    public R<Map<String, Object>> po(@RequestBody Map<String, Object> body) {
        return R.ok(service.createPo(body, "MANUAL"));
    }

    @GetMapping("/mm/po")
    public R<List<Map<String, Object>>> pos() {
        return R.ok(service.list("sap_purchase_order"));
    }

    @GetMapping("/mm/po/{id}")
    public R<Map<String, Object>> po(@PathVariable String id) {
        return R.ok(jdbc.queryForMap("SELECT * FROM sap_purchase_order WHERE ebeln=?", id));
    }

    @PostMapping("/mm/po/{id}/close")
    public R<Map<String, Object>> closePo(@PathVariable String id) {
        jdbc.update("UPDATE sap_purchase_order SET status='CLOSED' WHERE ebeln=?", id);
        return R.ok(jdbc.queryForMap("SELECT * FROM sap_purchase_order WHERE ebeln=?", id));
    }

    @PostMapping("/mm/migo")
    public R<Map<String, Object>> migo(@RequestBody Map<String, Object> body) {
        return R.ok(service.postMigo(body));
    }

    @GetMapping("/mm/material-docs")
    public R<List<Map<String, Object>>> materialDocs() {
        return R.ok(service.list("sap_material_document"));
    }

    @GetMapping("/mm/stock")
    public R<List<Map<String, Object>>> stock(@RequestParam(required = false) String matnr,
                                              @RequestParam(required = false) String werks) {
        if (matnr != null && werks != null) return R.ok(jdbc.queryForList("SELECT * FROM sap_stock WHERE matnr=? AND werks=?", matnr, werks));
        if (matnr != null) return R.ok(jdbc.queryForList("SELECT * FROM sap_stock WHERE matnr=?", matnr));
        return R.ok(service.list("sap_stock"));
    }

    @PostMapping("/mm/miro")
    public R<Map<String, Object>> miro(@RequestBody Map<String, Object> body) {
        return R.ok(service.postMiro(body));
    }

    @GetMapping("/mm/supplier-invoices")
    public R<List<Map<String, Object>>> invoices() {
        return R.ok(service.list("sap_supplier_invoice"));
    }

    @GetMapping("/mm/gr-ir")
    public R<List<Map<String, Object>>> grir() {
        return R.ok(service.list("sap_gr_ir"));
    }

    @GetMapping("/sd/customers")
    public R<List<Map<String, Object>>> customers() {
        return R.ok(service.list("sap_customer"));
    }

    @PostMapping("/sd/customers")
    public R<Map<String, Object>> customer(@RequestBody Map<String, Object> body) {
        return R.ok(service.saveMaster("sap_customer", "kunnr", body));
    }

    @PostMapping("/sd/so")
    public R<Map<String, Object>> so(@RequestBody Map<String, Object> body) {
        return R.ok(service.createSo(body));
    }

    @GetMapping("/sd/so")
    public R<List<Map<String, Object>>> sos() {
        return R.ok(service.list("sap_sales_order"));
    }

    @PostMapping("/sd/dn")
    public R<Map<String, Object>> dn(@RequestBody Map<String, Object> body) {
        return R.ok(service.createDn(body));
    }

    @PostMapping("/sd/dn/{id}/pick")
    public R<Map<String, Object>> pick(@PathVariable String id) {
        return R.ok(service.pickDn(id));
    }

    @PostMapping("/sd/dn/{id}/pgi")
    public R<Map<String, Object>> pgi(@PathVariable String id) {
        return R.ok(service.pgi(id));
    }

    @PostMapping("/sd/billing")
    public R<Map<String, Object>> billing(@RequestBody Map<String, Object> body) {
        return R.ok(service.bill(body));
    }

    @GetMapping("/sd/billing")
    public R<List<Map<String, Object>>> billings() {
        return R.ok(service.list("sap_billing_doc"));
    }

    @GetMapping("/fi/gl-accounts")
    public R<List<Map<String, Object>>> glAccounts() {
        return R.ok(service.list("sap_gl_account"));
    }

    @PostMapping("/fi/gl-accounts")
    public R<Map<String, Object>> glAccount(@RequestBody Map<String, Object> body) {
        return R.ok(service.saveMaster("sap_gl_account", "saknr", body));
    }

    @PostMapping("/fi/documents")
    public R<Map<String, Object>> fi(@RequestBody Map<String, Object> body) {
        return R.ok(service.createFi(body));
    }

    @GetMapping("/fi/documents")
    public R<List<Map<String, Object>>> fis(@RequestParam(required = false) String blart) {
        if (blart == null) return R.ok(service.list("sap_acc_document"));
        return R.ok(jdbc.queryForList("SELECT * FROM sap_acc_document WHERE blart=? ORDER BY id DESC", blart));
    }

    @GetMapping("/fi/documents/{id}")
    public R<Map<String, Object>> fi(@PathVariable String id) {
        Map<String, Object> doc = jdbc.queryForMap("SELECT * FROM sap_acc_document WHERE id=? OR belnr=?", id, id);
        doc.put("items", jdbc.queryForList("SELECT * FROM sap_acc_document_item WHERE belnr=?", doc.get("belnr")));
        return R.ok(doc);
    }

    @PostMapping("/fi/documents/{id}/reverse")
    public R<Map<String, Object>> reverse(@PathVariable String id) {
        return R.ok(service.reverseFi(id));
    }

    @PostMapping("/fi/payments")
    public R<Map<String, Object>> payment(@RequestBody Map<String, Object> body) {
        return R.ok(service.payment(body));
    }

    @GetMapping("/fi/balances")
    public R<List<Map<String, Object>>> balances() {
        return R.ok(jdbc.queryForList("SELECT saknr, SUM(CASE WHEN shkzg='S' THEN amount ELSE -amount END) balance FROM sap_acc_document_item GROUP BY saknr"));
    }

    @GetMapping("/fi/ap/open-items")
    public R<List<Map<String, Object>>> ap(@RequestParam(required = false) String lifnr) {
        return R.ok(lifnr == null ? jdbc.queryForList("SELECT * FROM sap_acc_document_item WHERE saknr='2201'")
                : jdbc.queryForList("SELECT * FROM sap_acc_document_item WHERE saknr='2201' AND lifnr=?", lifnr));
    }

    @GetMapping("/fi/ar/open-items")
    public R<List<Map<String, Object>>> ar(@RequestParam(required = false) String kunnr) {
        return R.ok(kunnr == null ? jdbc.queryForList("SELECT * FROM sap_acc_document_item WHERE saknr='1122'")
                : jdbc.queryForList("SELECT * FROM sap_acc_document_item WHERE saknr='1122' AND kunnr=?", kunnr));
    }

    @GetMapping("/fi/account-determination")
    public R<List<Map<String, Object>>> determination() {
        return R.ok(service.list("sap_account_determination"));
    }

    @PutMapping("/fi/account-determination")
    public R<Void> updateDetermination(@RequestBody Map<String, Object> body) {
        jdbc.update("UPDATE sap_account_determination SET saknr=? WHERE account_key=?", body.get("saknr"), body.get("accountKey"));
        return R.ok();
    }

    @GetMapping("/co/cost-centers")
    public R<List<Map<String, Object>>> costCenters() {
        return R.ok(service.list("sap_cost_center"));
    }

    @PostMapping("/co/cost-centers")
    public R<Map<String, Object>> costCenter(@RequestBody Map<String, Object> body) {
        return R.ok(service.saveMaster("sap_cost_center", "kostl", body));
    }

    @GetMapping("/co/documents")
    public R<List<Map<String, Object>>> coDocuments(@RequestParam(required = false) String kostl) {
        return R.ok(kostl == null ? service.list("sap_co_document")
                : jdbc.queryForList("SELECT * FROM sap_co_document WHERE kostl=?", kostl));
    }

    @GetMapping("/co/report")
    public R<List<Map<String, Object>>> coReport() {
        return R.ok(jdbc.queryForList("SELECT kostl,cost_element,SUM(amount) amount FROM sap_co_document GROUP BY kostl,cost_element"));
    }

    @GetMapping("/pp/bom")
    public R<List<Map<String, Object>>> boms() {
        return R.ok(service.list("sap_bom"));
    }

    @PostMapping("/pp/bom")
    public R<Map<String, Object>> bom(@RequestBody Map<String, Object> body) {
        return R.ok(service.createBom(body));
    }

    @PostMapping("/pp/orders")
    public R<Map<String, Object>> order(@RequestBody Map<String, Object> body) {
        return R.ok(service.createProductionOrder(body));
    }

    @GetMapping("/pp/orders")
    public R<List<Map<String, Object>>> orders() {
        return R.ok(service.list("sap_production_order"));
    }

    @PostMapping("/pp/orders/{id}/release")
    public R<Map<String, Object>> release(@PathVariable String id) {
        return R.ok(service.releaseProduction(id));
    }

    @PostMapping("/pp/orders/{id}/issue")
    public R<Map<String, Object>> issue(@PathVariable String id, @RequestBody(required = false) Map<String, Object> body) {
        return R.ok(service.issueProductionOrder(id, body == null ? new HashMap<String, Object>() : body));
    }

    @PostMapping("/pp/orders/{id}/confirm")
    public R<Map<String, Object>> confirm(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return R.ok(service.confirmProduction(id, body));
    }

    @PostMapping("/pp/orders/{id}/receipt")
    public R<Map<String, Object>> receipt(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return R.ok(service.receiptProduction(id, body));
    }

    @PostMapping("/pp/orders/{id}/teco")
    public R<Map<String, Object>> teco(@PathVariable String id) {
        return R.ok(service.teco(id));
    }

    @GetMapping("/integration/logs")
    public R<List<Map<String, Object>>> integrationLogs(@RequestParam(required = false) String system,
                                                        @RequestParam(required = false) String direction) {
        return R.ok(logs.list(system, direction));
    }

    @PostMapping("/integration/bms/deliveries/{dnId}/push")
    public R<Map<String, Object>> pushDelivery(@PathVariable String dnId) {
        return R.ok(service.pgi(dnId));
    }

    @GetMapping("/dashboard/summary")
    public R<Map<String, Object>> dashboard() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("purchaseOrders", jdbc.queryForObject("SELECT COUNT(*) FROM sap_purchase_order", Integer.class));
        result.put("pendingReceipts", jdbc.queryForObject("SELECT COUNT(*) FROM sap_purchase_order WHERE status IN ('OPEN','PARTIAL')", Integer.class));
        result.put("stockValue", jdbc.queryForObject("SELECT COALESCE(SUM(value),0) FROM sap_stock", Object.class));
        result.put("revenue", jdbc.queryForObject("SELECT COALESCE(SUM(amount),0) FROM sap_acc_document_item WHERE saknr='6001'", Object.class));
        result.put("cost", jdbc.queryForObject("SELECT COALESCE(SUM(amount),0) FROM sap_acc_document_item WHERE saknr IN ('6401','6402','5001')", Object.class));
        result.put("openAp", jdbc.queryForObject("SELECT COUNT(*) FROM sap_acc_document_item WHERE saknr='2201'", Integer.class));
        result.put("openAr", jdbc.queryForObject("SELECT COUNT(*) FROM sap_acc_document_item WHERE saknr='1122'", Integer.class));
        return R.ok(result);
    }

    private String orgTable(String kind) {
        if ("company-codes".equals(kind)) return "sap_company_code";
        if ("plants".equals(kind)) return "sap_plant";
        if ("storage-locations".equals(kind)) return "sap_storage_location";
        if ("purchasing-orgs".equals(kind)) return "sap_purchasing_org";
        if ("purchasing-groups".equals(kind)) return "sap_purchasing_group";
        if ("sales-orgs".equals(kind)) return "sap_sales_org";
        throw new IllegalArgumentException("未知组织类型: " + kind);
    }

    private static Map<String, Object> map(String k1, Object v1, String k2, Object v2) {
        Map<String, Object> m = new HashMap<>();
        m.put(k1, v1);
        m.put(k2, v2);
        return m;
    }
}
