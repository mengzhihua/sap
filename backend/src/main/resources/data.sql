INSERT INTO sap_company_code(bukrs,name,currency)
SELECT '1000','SAP 演示公司','CNY' WHERE NOT EXISTS (SELECT 1 FROM sap_company_code WHERE bukrs='1000');
INSERT INTO sap_plant(werks,name,bukrs,bms_warehouse_code)
SELECT '1000','上海工厂','1000','WH-SH' WHERE NOT EXISTS (SELECT 1 FROM sap_plant WHERE werks='1000');
INSERT INTO sap_plant(werks,name,bukrs,bms_warehouse_code)
SELECT '2000','北京工厂','1000','WH-BJ' WHERE NOT EXISTS (SELECT 1 FROM sap_plant WHERE werks='2000');
INSERT INTO sap_storage_location(werks,lgort,name)
SELECT '1000','0001','原材料仓' WHERE NOT EXISTS (SELECT 1 FROM sap_storage_location WHERE werks='1000' AND lgort='0001');
INSERT INTO sap_storage_location(werks,lgort,name)
SELECT '1000','0002','成品仓' WHERE NOT EXISTS (SELECT 1 FROM sap_storage_location WHERE werks='1000' AND lgort='0002');
INSERT INTO sap_storage_location(werks,lgort,name)
SELECT '2000','0001','北京仓' WHERE NOT EXISTS (SELECT 1 FROM sap_storage_location WHERE werks='2000' AND lgort='0001');
INSERT INTO sap_purchasing_org(ekorg,name)
SELECT '1000','采购组织 1000' WHERE NOT EXISTS (SELECT 1 FROM sap_purchasing_org WHERE ekorg='1000');
INSERT INTO sap_purchasing_group(ekgrp,name)
SELECT '001','采购组 001' WHERE NOT EXISTS (SELECT 1 FROM sap_purchasing_group WHERE ekgrp='001');
INSERT INTO sap_sales_org(vkorg,name,bukrs)
SELECT '1000','销售组织 1000','1000' WHERE NOT EXISTS (SELECT 1 FROM sap_sales_org WHERE vkorg='1000');

INSERT INTO sap_number_range(object_name,prefix,current_no)
SELECT 'PO','45',0 WHERE NOT EXISTS (SELECT 1 FROM sap_number_range WHERE object_name='PO');
INSERT INTO sap_number_range(object_name,prefix,current_no)
SELECT 'MATERIAL','50',0 WHERE NOT EXISTS (SELECT 1 FROM sap_number_range WHERE object_name='MATERIAL');
INSERT INTO sap_number_range(object_name,prefix,current_no)
SELECT 'INVOICE','51',0 WHERE NOT EXISTS (SELECT 1 FROM sap_number_range WHERE object_name='INVOICE');
INSERT INTO sap_number_range(object_name,prefix,current_no)
SELECT 'FI','1',0 WHERE NOT EXISTS (SELECT 1 FROM sap_number_range WHERE object_name='FI');
INSERT INTO sap_number_range(object_name,prefix,current_no)
SELECT 'SO','1',0 WHERE NOT EXISTS (SELECT 1 FROM sap_number_range WHERE object_name='SO');
INSERT INTO sap_number_range(object_name,prefix,current_no)
SELECT 'DN','8',0 WHERE NOT EXISTS (SELECT 1 FROM sap_number_range WHERE object_name='DN');
INSERT INTO sap_number_range(object_name,prefix,current_no)
SELECT 'BILLING','9',0 WHERE NOT EXISTS (SELECT 1 FROM sap_number_range WHERE object_name='BILLING');
INSERT INTO sap_number_range(object_name,prefix,current_no)
SELECT 'PRODORD','1000',0 WHERE NOT EXISTS (SELECT 1 FROM sap_number_range WHERE object_name='PRODORD');
INSERT INTO sap_number_range(object_name,prefix,current_no)
SELECT 'PR','10',0 WHERE NOT EXISTS (SELECT 1 FROM sap_number_range WHERE object_name='PR');

INSERT INTO sap_vendor(lifnr,name,alias_code,country,payment_term,recon_account)
SELECT '100010','华东电子','SUP01','CN','0001','2201' WHERE NOT EXISTS (SELECT 1 FROM sap_vendor WHERE lifnr='100010');
INSERT INTO sap_vendor(lifnr,name,alias_code,country,payment_term,recon_account)
SELECT '100020','华南工业','SUP02','CN','0001','2201' WHERE NOT EXISTS (SELECT 1 FROM sap_vendor WHERE lifnr='100020');
INSERT INTO sap_vendor(lifnr,name,alias_code,country,payment_term,recon_account)
SELECT '100030','北方元件','SUP03','CN','0001','2201' WHERE NOT EXISTS (SELECT 1 FROM sap_vendor WHERE lifnr='100030');
INSERT INTO sap_material(matnr,maktx,meins,mtart,matkl,std_price,price_control,alias_code)
SELECT 'M1001','电子元件 A','EA','ROH','RAW',45.00,'S','SKU001' WHERE NOT EXISTS (SELECT 1 FROM sap_material WHERE matnr='M1001');
INSERT INTO sap_material(matnr,maktx,meins,mtart,matkl,std_price,price_control,alias_code)
SELECT 'M1002','电子元件 B','EA','ROH','RAW',12.00,'S','SKU002' WHERE NOT EXISTS (SELECT 1 FROM sap_material WHERE matnr='M1002');
INSERT INTO sap_material(matnr,maktx,meins,mtart,matkl,std_price,price_control,alias_code)
SELECT 'M1003','螺钉','EA','ROH','RAW',2.50,'S','SKU003' WHERE NOT EXISTS (SELECT 1 FROM sap_material WHERE matnr='M1003');
INSERT INTO sap_material(matnr,maktx,meins,mtart,matkl,std_price,price_control,alias_code)
SELECT 'M1004','半成品组件','EA','HALB','SEMI',80.00,'S','SKU004' WHERE NOT EXISTS (SELECT 1 FROM sap_material WHERE matnr='M1004');
INSERT INTO sap_material(matnr,maktx,meins,mtart,matkl,std_price,price_control,alias_code)
SELECT 'F2001','成品控制器','EA','FERT','FIN',300.00,'S','F2001' WHERE NOT EXISTS (SELECT 1 FROM sap_material WHERE matnr='F2001');
INSERT INTO sap_customer(kunnr,name,alias_code,recon_account)
SELECT '200010','上海客户','CUST-001','1122' WHERE NOT EXISTS (SELECT 1 FROM sap_customer WHERE kunnr='200010');
INSERT INTO sap_customer(kunnr,name,alias_code,recon_account)
SELECT '200020','北京客户','CUST-002','1122' WHERE NOT EXISTS (SELECT 1 FROM sap_customer WHERE kunnr='200020');
INSERT INTO sap_customer(kunnr,name,alias_code,recon_account)
SELECT '200030','广州客户','CUST-003','1122' WHERE NOT EXISTS (SELECT 1 FROM sap_customer WHERE kunnr='200030');
INSERT INTO sap_cost_center(kostl,name,bukrs,responsible)
SELECT 'CC1000','生产部','1000','生产经理' WHERE NOT EXISTS (SELECT 1 FROM sap_cost_center WHERE kostl='CC1000');
INSERT INTO sap_cost_center(kostl,name,bukrs,responsible)
SELECT 'CC2000','管理部','1000','行政经理' WHERE NOT EXISTS (SELECT 1 FROM sap_cost_center WHERE kostl='CC2000');
INSERT INTO sap_cost_center(kostl,name,bukrs,responsible)
SELECT 'CC3000','销售部','1000','销售经理' WHERE NOT EXISTS (SELECT 1 FROM sap_cost_center WHERE kostl='CC3000');

INSERT INTO sap_gl_account(saknr,txt,type,recon_type) SELECT '1001','库存现金','ASSET',NULL WHERE NOT EXISTS (SELECT 1 FROM sap_gl_account WHERE saknr='1001');
INSERT INTO sap_gl_account(saknr,txt,type,recon_type) SELECT '1002','银行存款','ASSET',NULL WHERE NOT EXISTS (SELECT 1 FROM sap_gl_account WHERE saknr='1002');
INSERT INTO sap_gl_account(saknr,txt,type,recon_type) SELECT '1122','应收账款','ASSET','D' WHERE NOT EXISTS (SELECT 1 FROM sap_gl_account WHERE saknr='1122');
INSERT INTO sap_gl_account(saknr,txt,type,recon_type) SELECT '1405','库存商品','ASSET',NULL WHERE NOT EXISTS (SELECT 1 FROM sap_gl_account WHERE saknr='1405');
INSERT INTO sap_gl_account(saknr,txt,type,recon_type) SELECT '1411','原材料','ASSET',NULL WHERE NOT EXISTS (SELECT 1 FROM sap_gl_account WHERE saknr='1411');
INSERT INTO sap_gl_account(saknr,txt,type,recon_type) SELECT '2201','应付账款','LIABILITY','K' WHERE NOT EXISTS (SELECT 1 FROM sap_gl_account WHERE saknr='2201');
INSERT INTO sap_gl_account(saknr,txt,type,recon_type) SELECT '2202','GR/IR 暂估','LIABILITY','K' WHERE NOT EXISTS (SELECT 1 FROM sap_gl_account WHERE saknr='2202');
INSERT INTO sap_gl_account(saknr,txt,type,recon_type) SELECT '2211','进项税','LIABILITY',NULL WHERE NOT EXISTS (SELECT 1 FROM sap_gl_account WHERE saknr='2211');
INSERT INTO sap_gl_account(saknr,txt,type,recon_type) SELECT '2221','销项税','LIABILITY',NULL WHERE NOT EXISTS (SELECT 1 FROM sap_gl_account WHERE saknr='2221');
INSERT INTO sap_gl_account(saknr,txt,type,recon_type) SELECT '4001','实收资本','EQUITY',NULL WHERE NOT EXISTS (SELECT 1 FROM sap_gl_account WHERE saknr='4001');
INSERT INTO sap_gl_account(saknr,txt,type,recon_type) SELECT '5001','生产成本','EXPENSE',NULL WHERE NOT EXISTS (SELECT 1 FROM sap_gl_account WHERE saknr='5001');
INSERT INTO sap_gl_account(saknr,txt,type,recon_type) SELECT '6001','主营业务收入','REVENUE',NULL WHERE NOT EXISTS (SELECT 1 FROM sap_gl_account WHERE saknr='6001');
INSERT INTO sap_gl_account(saknr,txt,type,recon_type) SELECT '6401','制造费用/物料消耗','EXPENSE',NULL WHERE NOT EXISTS (SELECT 1 FROM sap_gl_account WHERE saknr='6401');
INSERT INTO sap_gl_account(saknr,txt,type,recon_type) SELECT '6402','主营业务成本','EXPENSE',NULL WHERE NOT EXISTS (SELECT 1 FROM sap_gl_account WHERE saknr='6402');
INSERT INTO sap_gl_account(saknr,txt,type,recon_type) SELECT '6403','价差','EXPENSE',NULL WHERE NOT EXISTS (SELECT 1 FROM sap_gl_account WHERE saknr='6403');
INSERT INTO sap_gl_account(saknr,txt,type,recon_type) SELECT '6601','管理费用','EXPENSE',NULL WHERE NOT EXISTS (SELECT 1 FROM sap_gl_account WHERE saknr='6601');

INSERT INTO sap_account_determination(account_key,saknr) SELECT 'BSX_ROH','1411' WHERE NOT EXISTS (SELECT 1 FROM sap_account_determination WHERE account_key='BSX_ROH');
INSERT INTO sap_account_determination(account_key,saknr) SELECT 'BSX_FERT','1405' WHERE NOT EXISTS (SELECT 1 FROM sap_account_determination WHERE account_key='BSX_FERT');
INSERT INTO sap_account_determination(account_key,saknr) SELECT 'BSX','1405' WHERE NOT EXISTS (SELECT 1 FROM sap_account_determination WHERE account_key='BSX');
INSERT INTO sap_account_determination(account_key,saknr) SELECT 'WRX','2202' WHERE NOT EXISTS (SELECT 1 FROM sap_account_determination WHERE account_key='WRX');
INSERT INTO sap_account_determination(account_key,saknr) SELECT 'GBB_VBR','6401' WHERE NOT EXISTS (SELECT 1 FROM sap_account_determination WHERE account_key='GBB_VBR');
INSERT INTO sap_account_determination(account_key,saknr) SELECT 'GBB_VAX','6402' WHERE NOT EXISTS (SELECT 1 FROM sap_account_determination WHERE account_key='GBB_VAX');
INSERT INTO sap_account_determination(account_key,saknr) SELECT 'GBB_ZOF','5001' WHERE NOT EXISTS (SELECT 1 FROM sap_account_determination WHERE account_key='GBB_ZOF');
INSERT INTO sap_account_determination(account_key,saknr) SELECT 'PRD','6403' WHERE NOT EXISTS (SELECT 1 FROM sap_account_determination WHERE account_key='PRD');
INSERT INTO sap_account_determination(account_key,saknr) SELECT 'ERL','6001' WHERE NOT EXISTS (SELECT 1 FROM sap_account_determination WHERE account_key='ERL');
INSERT INTO sap_account_determination(account_key,saknr) SELECT 'MWS','2221' WHERE NOT EXISTS (SELECT 1 FROM sap_account_determination WHERE account_key='MWS');
INSERT INTO sap_account_determination(account_key,saknr) SELECT 'VST','2211' WHERE NOT EXISTS (SELECT 1 FROM sap_account_determination WHERE account_key='VST');
INSERT INTO sap_account_determination(account_key,saknr) SELECT 'AP','2201' WHERE NOT EXISTS (SELECT 1 FROM sap_account_determination WHERE account_key='AP');
INSERT INTO sap_account_determination(account_key,saknr) SELECT 'AR','1122' WHERE NOT EXISTS (SELECT 1 FROM sap_account_determination WHERE account_key='AR');
INSERT INTO sap_account_determination(account_key,saknr) SELECT 'BANK','1002' WHERE NOT EXISTS (SELECT 1 FROM sap_account_determination WHERE account_key='BANK');
INSERT INTO sap_account_determination(account_key,saknr) SELECT 'ASSET_APC','1601' WHERE NOT EXISTS (SELECT 1 FROM sap_account_determination WHERE account_key='ASSET_APC');
INSERT INTO sap_account_determination(account_key,saknr) SELECT 'ASSET_ACCUM_DEP','1602' WHERE NOT EXISTS (SELECT 1 FROM sap_account_determination WHERE account_key='ASSET_ACCUM_DEP');
INSERT INTO sap_account_determination(account_key,saknr) SELECT 'DEPRECIATION','6602' WHERE NOT EXISTS (SELECT 1 FROM sap_account_determination WHERE account_key='DEPRECIATION');
INSERT INTO sap_gl_account(saknr,txt,type,recon_type) SELECT '1601','固定资产原值','ASSET',NULL WHERE NOT EXISTS (SELECT 1 FROM sap_gl_account WHERE saknr='1601');
INSERT INTO sap_gl_account(saknr,txt,type,recon_type) SELECT '1602','累计折旧','ASSET',NULL WHERE NOT EXISTS (SELECT 1 FROM sap_gl_account WHERE saknr='1602');
INSERT INTO sap_gl_account(saknr,txt,type,recon_type) SELECT '6602','折旧费用','EXPENSE',NULL WHERE NOT EXISTS (SELECT 1 FROM sap_gl_account WHERE saknr='6602');
INSERT INTO sap_number_range(object_name,prefix,current_no) SELECT 'ASSET','A',100000 WHERE NOT EXISTS (SELECT 1 FROM sap_number_range WHERE object_name='ASSET');

INSERT INTO sap_stock(matnr,werks,lgort,unrestricted_qty,value)
SELECT 'M1001','1000','0001',500,22500 WHERE NOT EXISTS (SELECT 1 FROM sap_stock WHERE matnr='M1001' AND werks='1000' AND lgort='0001');
INSERT INTO sap_stock(matnr,werks,lgort,unrestricted_qty,value)
SELECT 'M1002','1000','0001',300,3600 WHERE NOT EXISTS (SELECT 1 FROM sap_stock WHERE matnr='M1002' AND werks='1000' AND lgort='0001');
INSERT INTO sap_stock(matnr,werks,lgort,unrestricted_qty,value)
SELECT 'M1003','1000','0001',2000,5000 WHERE NOT EXISTS (SELECT 1 FROM sap_stock WHERE matnr='M1003' AND werks='1000' AND lgort='0001');
INSERT INTO sap_stock(matnr,werks,lgort,unrestricted_qty,value)
SELECT 'M1004','1000','0001',100,8000 WHERE NOT EXISTS (SELECT 1 FROM sap_stock WHERE matnr='M1004' AND werks='1000' AND lgort='0001');
INSERT INTO sap_stock(matnr,werks,lgort,unrestricted_qty,value)
SELECT 'F2001','1000','0002',20,6000 WHERE NOT EXISTS (SELECT 1 FROM sap_stock WHERE matnr='F2001' AND werks='1000' AND lgort='0002');

INSERT INTO sap_bom(matnr,werks,base_qty)
SELECT 'F2001','1000',1 WHERE NOT EXISTS (SELECT 1 FROM sap_bom WHERE matnr='F2001' AND werks='1000');
INSERT INTO sap_bom_item(matnr,werks,component,qty)
SELECT 'F2001','1000','M1001',2 WHERE NOT EXISTS (SELECT 1 FROM sap_bom_item WHERE matnr='F2001' AND werks='1000' AND component='M1001');
INSERT INTO sap_bom_item(matnr,werks,component,qty)
SELECT 'F2001','1000','M1004',1 WHERE NOT EXISTS (SELECT 1 FROM sap_bom_item WHERE matnr='F2001' AND werks='1000' AND component='M1004');
INSERT INTO sap_bom_item(matnr,werks,component,qty)
SELECT 'F2001','1000','M1003',4 WHERE NOT EXISTS (SELECT 1 FROM sap_bom_item WHERE matnr='F2001' AND werks='1000' AND component='M1003');

INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'MM01','MM','创建物料','/mm/materials' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='MM01');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'MM03','MM','物料显示','/mm/materials' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='MM03');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'MK01','MM','创建供应商','/mm/vendors' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='MK01');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'ME51N','MM','采购申请','/mm/pr' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='ME51N');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'ME21N','MM','采购订单','/mm/po' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='ME21N');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'MIGO','MM','物料凭证','/mm/migo' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='MIGO');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'MMBE','MM','库存概览','/mm/stock' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='MMBE');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'MIRO','MM','发票校验','/mm/miro' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='MIRO');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'VA01','SD','销售订单','/sd/so' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='VA01');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'VL01N','SD','外向交货','/sd/dn' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='VL01N');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'VF01','SD','开票','/sd/billing' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='VF01');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'FS00','FI','科目','/fi/gl-accounts' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='FS00');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'FB50','FI','总账凭证','/fi/documents' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='FB50');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'F-53','FI','付款','/fi/payments' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='F-53');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'F-28','FI','收款','/fi/payments' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='F-28');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'FB08','FI','冲销','/fi/documents' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='FB08');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'KS01','CO','成本中心','/co/cost-centers' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='KS01');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'CS01','PP','BOM','/pp/bom' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='CS01');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'CO01','PP','生产订单','/pp/orders' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='CO01');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'CO11N','PP','报工','/pp/orders' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='CO11N');
UPDATE sap_tcode SET route='/mm/materials' WHERE tcode IN ('MM01','MM03');
UPDATE sap_tcode SET route='/mm/vendors' WHERE tcode='MK01';
UPDATE sap_tcode SET route='/mm/po' WHERE tcode IN ('ME21N','ME23N');
UPDATE sap_tcode SET route='/mm/material-docs' WHERE tcode='MB51';
UPDATE sap_tcode SET route='/sd/customers' WHERE tcode='VD01';
UPDATE sap_tcode SET route='/fi/gl-accounts' WHERE tcode='FS00';
UPDATE sap_tcode SET route='/fi/fb50' WHERE tcode='FB50';
UPDATE sap_tcode SET route='/fi/balances' WHERE tcode='FAGLB03';
UPDATE sap_tcode SET route='/fi/ap' WHERE tcode='FBL1N';
UPDATE sap_tcode SET route='/fi/ar' WHERE tcode='FBL5N';
UPDATE sap_tcode SET route='/co/documents' WHERE tcode='KSB1';
UPDATE sap_tcode SET route='/co/report' WHERE tcode='S_ALR_87013611';
UPDATE sap_tcode SET route='/basis/org' WHERE tcode='SPRO';
UPDATE sap_tcode SET route='/basis/tcodes' WHERE tcode='SE16';
UPDATE sap_tcode SET route='/basis/op-logs' WHERE tcode='SM37';
UPDATE sap_tcode SET route='/integration/logs' WHERE tcode='SLG1';
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'ME23N','MM','采购订单显示','/mm/po' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='ME23N');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'MB51','MM','物料凭证','/mm/material-docs' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='MB51');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'VD01','SD','客户主数据','/sd/customers' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='VD01');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'FAGLB03','FI','余额报表','/fi/balances' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='FAGLB03');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'FBL1N','FI','应付未清项','/fi/ap' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='FBL1N');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'FBL5N','FI','应收未清项','/fi/ar' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='FBL5N');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'KSB1','CO','CO凭证','/co/documents' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='KSB1');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'S_ALR_87013611','CO','成本中心报表','/co/report' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='S_ALR_87013611');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'CO02','PP','修改生产订单','/pp/orders' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='CO02');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'COOIS','PP','生产订单信息','/pp/orders' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='COOIS');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'SU01','BASIS','用户管理','/basis/users' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='SU01');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'SE16','BASIS','事务代码目录','/basis/tcodes' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='SE16');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'SM37','BASIS','操作日志','/basis/op-logs' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='SM37');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'SPRO','BASIS','组织结构','/basis/org' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='SPRO');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'SLG1','INTEGRATION','集成日志','/integration/logs' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='SLG1');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'AS01','FI-AA','创建固定资产','/fi/assets' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='AS01');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'AW01N','FI-AA','资产浏览器','/fi/assets' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='AW01N');
INSERT INTO sap_tcode(tcode,module,name,route) SELECT 'AFAB','FI-AA','折旧运行','/fi/assets' WHERE NOT EXISTS (SELECT 1 FROM sap_tcode WHERE tcode='AFAB');
