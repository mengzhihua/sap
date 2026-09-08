CREATE TABLE IF NOT EXISTS sap_user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(512) NOT NULL,
  real_name VARCHAR(128),
  role VARCHAR(32) NOT NULL,
  status INT NOT NULL DEFAULT 1,
  last_login_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS sap_role (id BIGINT AUTO_INCREMENT PRIMARY KEY, code VARCHAR(32) UNIQUE, name VARCHAR(128));
CREATE TABLE IF NOT EXISTS sap_user_role (user_id BIGINT, role_id BIGINT, PRIMARY KEY(user_id,role_id));
CREATE TABLE IF NOT EXISTS sap_op_log (id BIGINT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(64), method VARCHAR(16), path VARCHAR(255), request_body TEXT, response_body TEXT, created_at DATETIME DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE IF NOT EXISTS sap_company_code (bukrs VARCHAR(16) PRIMARY KEY, name VARCHAR(128), currency VARCHAR(8));
CREATE TABLE IF NOT EXISTS sap_plant (werks VARCHAR(16) PRIMARY KEY, name VARCHAR(128), bukrs VARCHAR(16), bms_warehouse_code VARCHAR(64));
CREATE TABLE IF NOT EXISTS sap_storage_location (werks VARCHAR(16), lgort VARCHAR(16), name VARCHAR(128), PRIMARY KEY(werks,lgort));
CREATE TABLE IF NOT EXISTS sap_purchasing_org (ekorg VARCHAR(16) PRIMARY KEY, name VARCHAR(128));
CREATE TABLE IF NOT EXISTS sap_purchasing_group (ekgrp VARCHAR(16) PRIMARY KEY, name VARCHAR(128));
CREATE TABLE IF NOT EXISTS sap_sales_org (vkorg VARCHAR(16) PRIMARY KEY, name VARCHAR(128), bukrs VARCHAR(16));
CREATE TABLE IF NOT EXISTS sap_number_range (object_name VARCHAR(32) PRIMARY KEY, prefix VARCHAR(16), current_no INT NOT NULL DEFAULT 0);
CREATE TABLE IF NOT EXISTS sap_tcode (tcode VARCHAR(32) PRIMARY KEY, module VARCHAR(32), name VARCHAR(128), route VARCHAR(255));

CREATE TABLE IF NOT EXISTS sap_material (
  matnr VARCHAR(64) PRIMARY KEY, maktx VARCHAR(255), meins VARCHAR(16), mtart VARCHAR(16),
  matkl VARCHAR(64), std_price DECIMAL(18,2), price_control VARCHAR(1), alias_code VARCHAR(64) UNIQUE
);
CREATE TABLE IF NOT EXISTS sap_vendor (
  lifnr VARCHAR(64) PRIMARY KEY, name VARCHAR(255), alias_code VARCHAR(64) UNIQUE,
  country VARCHAR(32), payment_term VARCHAR(32), recon_account VARCHAR(32)
);
CREATE TABLE IF NOT EXISTS sap_purchase_req (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, banfn VARCHAR(32) UNIQUE, status VARCHAR(32),
  requester VARCHAR(64), bukrs VARCHAR(16), werks VARCHAR(16), total_amount DECIMAL(18,2),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS sap_purchase_req_item (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, banfn VARCHAR(32), bnfpo VARCHAR(16), matnr VARCHAR(64),
  menge DECIMAL(18,3), netpr DECIMAL(18,2), werks VARCHAR(16), lgort VARCHAR(16)
);
CREATE TABLE IF NOT EXISTS sap_purchase_order (
  ebeln VARCHAR(64) PRIMARY KEY, bsart VARCHAR(16), lifnr VARCHAR(64), ekorg VARCHAR(16),
  ekgrp VARCHAR(16), bukrs VARCHAR(16), waers VARCHAR(8), status VARCHAR(32),
  external_ref VARCHAR(128), source VARCHAR(32), total_amount DECIMAL(18,2),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS sap_purchase_order_item (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, ebeln VARCHAR(64), ebelp VARCHAR(16), matnr VARCHAR(64),
  werks VARCHAR(16), lgort VARCHAR(16), menge DECIMAL(18,3), netpr DECIMAL(18,2),
  delivered_qty DECIMAL(18,3) DEFAULT 0, invoiced_qty DECIMAL(18,3) DEFAULT 0, delivery_date DATE
);
CREATE TABLE IF NOT EXISTS sap_material_document (
  mblnr VARCHAR(64) PRIMARY KEY, mjahr VARCHAR(8), budat DATE, bwart VARCHAR(8),
  ref_type VARCHAR(32), ref_no VARCHAR(128), fi_belnr VARCHAR(64), created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS sap_material_document_item (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, mblnr VARCHAR(64), zeile VARCHAR(16), matnr VARCHAR(64),
  werks VARCHAR(16), lgort VARCHAR(16), menge DECIMAL(18,3), amount DECIMAL(18,2),
  bwart VARCHAR(8), ebeln VARCHAR(64), ebelp VARCHAR(16), kostl VARCHAR(32), aufnr VARCHAR(64), to_lgort VARCHAR(16)
);
CREATE TABLE IF NOT EXISTS sap_stock (
  matnr VARCHAR(64), werks VARCHAR(16), lgort VARCHAR(16), unrestricted_qty DECIMAL(18,3) DEFAULT 0,
  value DECIMAL(18,2) DEFAULT 0, PRIMARY KEY(matnr,werks,lgort)
);
CREATE TABLE IF NOT EXISTS sap_supplier_invoice (
  belnr VARCHAR(64) PRIMARY KEY, gjahr VARCHAR(8), lifnr VARCHAR(64), ebeln VARCHAR(64),
  external_invoice_no VARCHAR(128), gross_amount DECIMAL(18,2), tax_amount DECIMAL(18,2),
  status VARCHAR(32), fi_belnr VARCHAR(64), match_result VARCHAR(255), created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS sap_supplier_invoice_item (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, belnr VARCHAR(64), ebelp VARCHAR(16), qty DECIMAL(18,3), price DECIMAL(18,2)
);
CREATE TABLE IF NOT EXISTS sap_gr_ir (
  ebeln VARCHAR(64), ebelp VARCHAR(16), gr_qty DECIMAL(18,3) DEFAULT 0, ir_qty DECIMAL(18,3) DEFAULT 0,
  gr_amount DECIMAL(18,2) DEFAULT 0, ir_amount DECIMAL(18,2) DEFAULT 0, PRIMARY KEY(ebeln,ebelp)
);

CREATE TABLE IF NOT EXISTS sap_customer (kunnr VARCHAR(64) PRIMARY KEY, name VARCHAR(255), alias_code VARCHAR(64) UNIQUE, recon_account VARCHAR(32));
CREATE TABLE IF NOT EXISTS sap_sales_order (vbeln VARCHAR(64) PRIMARY KEY, auart VARCHAR(16), kunnr VARCHAR(64), vkorg VARCHAR(16), waers VARCHAR(8), status VARCHAR(32), created_at DATETIME DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE IF NOT EXISTS sap_sales_order_item (id BIGINT AUTO_INCREMENT PRIMARY KEY, vbeln VARCHAR(64), posnr VARCHAR(16), matnr VARCHAR(64), werks VARCHAR(16), kwmeng DECIMAL(18,3), netpr DECIMAL(18,2), delivered_qty DECIMAL(18,3) DEFAULT 0, billed_qty DECIMAL(18,3) DEFAULT 0);
CREATE TABLE IF NOT EXISTS sap_delivery (vbeln VARCHAR(64) PRIMARY KEY, so_vbeln VARCHAR(64), kunnr VARCHAR(64), werks VARCHAR(16), status VARCHAR(32), material_doc VARCHAR(64), bms_synced INT DEFAULT 0, bms_doc_no VARCHAR(128));
CREATE TABLE IF NOT EXISTS sap_delivery_item (id BIGINT AUTO_INCREMENT PRIMARY KEY, vbeln VARCHAR(64), posnr VARCHAR(16), matnr VARCHAR(64), qty DECIMAL(18,3), picked_qty DECIMAL(18,3) DEFAULT 0, pgi_qty DECIMAL(18,3) DEFAULT 0);
CREATE TABLE IF NOT EXISTS sap_billing_doc (vbeln VARCHAR(64) PRIMARY KEY, fkart VARCHAR(16), kunnr VARCHAR(64), net DECIMAL(18,2), tax DECIMAL(18,2), gross DECIMAL(18,2), fi_belnr VARCHAR(64), status VARCHAR(32));
CREATE TABLE IF NOT EXISTS sap_billing_doc_item (id BIGINT AUTO_INCREMENT PRIMARY KEY, vbeln VARCHAR(64), posnr VARCHAR(16), matnr VARCHAR(64), qty DECIMAL(18,3), netpr DECIMAL(18,2));

CREATE TABLE IF NOT EXISTS sap_gl_account (saknr VARCHAR(32) PRIMARY KEY, txt VARCHAR(255), type VARCHAR(32), recon_type VARCHAR(8));
CREATE TABLE IF NOT EXISTS sap_acc_document (id BIGINT AUTO_INCREMENT PRIMARY KEY, belnr VARCHAR(64) UNIQUE, gjahr VARCHAR(8), bukrs VARCHAR(16), blart VARCHAR(8), budat DATE, bldat DATE, waers VARCHAR(8), header_text VARCHAR(255), ref_no VARCHAR(128), source VARCHAR(32), reversed_by VARCHAR(64), cleared_by VARCHAR(64), created_at DATETIME DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE IF NOT EXISTS sap_acc_document_item (id BIGINT AUTO_INCREMENT PRIMARY KEY, belnr VARCHAR(64), buzei VARCHAR(16), bschl VARCHAR(8), shkzg VARCHAR(1), saknr VARCHAR(32), lifnr VARCHAR(64), kunnr VARCHAR(64), kostl VARCHAR(32), amount DECIMAL(18,2), text VARCHAR(255));
CREATE TABLE IF NOT EXISTS sap_payment (id BIGINT AUTO_INCREMENT PRIMARY KEY, type VARCHAR(8), partner VARCHAR(64), amount DECIMAL(18,2), belnr VARCHAR(64), cleared_docs VARCHAR(1000), created_at DATETIME DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE IF NOT EXISTS sap_account_determination (account_key VARCHAR(32) PRIMARY KEY, saknr VARCHAR(32));
CREATE TABLE IF NOT EXISTS sap_posting_period (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, bukrs VARCHAR(16) NOT NULL, fiscal_year INT NOT NULL,
  from_period INT NOT NULL, to_period INT NOT NULL, open INT NOT NULL DEFAULT 1,
  UNIQUE(bukrs,fiscal_year,from_period,to_period)
);

CREATE TABLE IF NOT EXISTS sap_cost_center (kostl VARCHAR(32) PRIMARY KEY, name VARCHAR(128), bukrs VARCHAR(16), responsible VARCHAR(128));
CREATE TABLE IF NOT EXISTS sap_co_document (id BIGINT AUTO_INCREMENT PRIMARY KEY, fi_belnr VARCHAR(64), kostl VARCHAR(32), cost_element VARCHAR(32), amount DECIMAL(18,2), budat DATE, text VARCHAR(255));
CREATE TABLE IF NOT EXISTS sap_bom (matnr VARCHAR(64), werks VARCHAR(16), base_qty DECIMAL(18,3), PRIMARY KEY(matnr,werks));
CREATE TABLE IF NOT EXISTS sap_bom_item (id BIGINT AUTO_INCREMENT PRIMARY KEY, matnr VARCHAR(64), werks VARCHAR(16), component VARCHAR(64), qty DECIMAL(18,3));
CREATE TABLE IF NOT EXISTS sap_production_order (aufnr VARCHAR(64) PRIMARY KEY, matnr VARCHAR(64), werks VARCHAR(16), target_qty DECIMAL(18,3), delivered_qty DECIMAL(18,3) DEFAULT 0, status VARCHAR(32), planned_cost DECIMAL(18,2), actual_cost DECIMAL(18,2) DEFAULT 0);
CREATE TABLE IF NOT EXISTS sap_production_order_component (id BIGINT AUTO_INCREMENT PRIMARY KEY, aufnr VARCHAR(64), matnr VARCHAR(64), req_qty DECIMAL(18,3), issued_qty DECIMAL(18,3) DEFAULT 0);
CREATE TABLE IF NOT EXISTS sap_confirmation (id BIGINT AUTO_INCREMENT PRIMARY KEY, aufnr VARCHAR(64), qty DECIMAL(18,3), budat DATE, created_at DATETIME DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE IF NOT EXISTS sap_vendor_evaluation (id BIGINT AUTO_INCREMENT PRIMARY KEY, lifnr VARCHAR(64), period VARCHAR(32), score DECIMAL(18,2), grade VARCHAR(16), UNIQUE(lifnr,period));
CREATE TABLE IF NOT EXISTS sap_integration_log (id BIGINT AUTO_INCREMENT PRIMARY KEY, direction VARCHAR(8), system_name VARCHAR(32), action_name VARCHAR(64), request TEXT, response TEXT, success INT, error TEXT, elapsed_ms BIGINT, created_at DATETIME DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE IF NOT EXISTS sap_bms_statement (id BIGINT AUTO_INCREMENT PRIMARY KEY, statement_no VARCHAR(128) UNIQUE, direction VARCHAR(8), partner_code VARCHAR(64), amount DECIMAL(18,2), tax_amount DECIMAL(18,2), biz_date DATE, remark VARCHAR(255), belnr VARCHAR(64), created_at DATETIME DEFAULT CURRENT_TIMESTAMP);

-- FI-AA 固定资产台账与价值变动
CREATE TABLE IF NOT EXISTS sap_fixed_asset (
  anln1 VARCHAR(64) PRIMARY KEY, name VARCHAR(255) NOT NULL, asset_class VARCHAR(32) NOT NULL,
  bukrs VARCHAR(16) NOT NULL, kostl VARCHAR(32), capitalization_date DATE,
  useful_life_months INT NOT NULL, acquisition_value DECIMAL(18,2) DEFAULT 0,
  accumulated_depreciation DECIMAL(18,2) DEFAULT 0, book_value DECIMAL(18,2) DEFAULT 0,
  salvage_value DECIMAL(18,2) DEFAULT 0, status VARCHAR(32) DEFAULT 'CREATED',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS sap_asset_transaction (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, anln1 VARCHAR(64) NOT NULL, transaction_type VARCHAR(32) NOT NULL,
  fiscal_period VARCHAR(16), posting_date DATE NOT NULL, amount DECIMAL(18,2) NOT NULL,
  belnr VARCHAR(64), text VARCHAR(255), created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(anln1, transaction_type, fiscal_period)
);
