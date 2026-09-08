export const groups = [
  {
    name: 'MM 物料管理',
    children: [
      ['MM01', '物料主数据', '/mm/materials', () => import('../views/mm/MaterialList.vue')],
      ['MK01', '供应商主数据', '/mm/vendors', () => import('../views/mm/VendorList.vue')],
      ['ME51N', '采购申请', '/mm/pr', () => import('../views/mm/PurchaseReqList.vue')],
      ['ME21N', '采购订单', '/mm/po', () => import('../views/mm/PurchaseOrderList.vue')],
      ['ME23N', '采购订单显示', '/mm/po', () => import('../views/mm/PurchaseOrderList.vue')],
      ['MIGO', '货物移动', '/mm/migo', () => import('../views/mm/Migo.vue')],
      ['MMBE', '库存总览', '/mm/stock', () => import('../views/mm/StockOverview.vue')],
      ['MIRO', '发票校验', '/mm/miro', () => import('../views/mm/Miro.vue')],
      ['MB51', '物料凭证', '/mm/material-docs', () => import('../views/mm/MaterialDocList.vue')],
      ['MIR4', '供应商发票', '/mm/invoices', () => import('../views/mm/SupplierInvoiceList.vue')],
      ['GRIR', 'GR/IR', '/mm/gr-ir', () => import('../views/mm/GrIrList.vue')],
      ['ME63', '供应商评价', '/mm/evaluations', () => import('../views/mm/VendorEvaluationList.vue')],
    ],
  },
  {
    name: 'SD 销售管理',
    children: [
      ['VD01', '客户主数据', '/sd/customers', () => import('../views/sd/CustomerList.vue')],
      ['VA01', '销售订单', '/sd/so', () => import('../views/sd/SalesOrderList.vue')],
      ['VL01N', '外向交货', '/sd/dn', () => import('../views/sd/DeliveryList.vue')],
      ['VF01', '开票', '/sd/billing', () => import('../views/sd/BillingList.vue')],
    ],
  },
  {
    name: 'FI 财务会计',
    children: [
      ['FS00', '总账科目', '/fi/gl-accounts', () => import('../views/fi/GlAccountList.vue')],
      ['FB50', '总账凭证', '/fi/fb50', () => import('../views/fi/JournalEntry.vue')],
      ['FB03', '财务凭证', '/fi/documents', () => import('../views/fi/DocumentList.vue')],
      ['F-53', '付款', '/fi/payments', () => import('../views/fi/Payments.vue')],
      ['F-28', '收款', '/fi/payments', () => import('../views/fi/Payments.vue')],
      ['FB08', '冲销凭证', '/fi/documents', () => import('../views/fi/DocumentList.vue')],
      ['FAGLB03', '余额报表', '/fi/balances', () => import('../views/fi/Balances.vue')],
      ['FBL1N', '应付未清项', '/fi/ap', () => import('../views/fi/OpenItemsAp.vue')],
      ['FBL5N', '应收未清项', '/fi/ar', () => import('../views/fi/OpenItemsAr.vue')],
      ['OBYC', '科目确定', '/fi/determination', () => import('../views/fi/AccountDetermination.vue')],
      ['AS01', '固定资产', '/fi/assets', () => import('../views/fi/FixedAssetList.vue')],
      ['AW01N', '资产浏览器', '/fi/assets', () => import('../views/fi/FixedAssetList.vue')],
      ['AFAB', '折旧运行', '/fi/assets', () => import('../views/fi/FixedAssetList.vue')],
    ],
  },
  {
    name: 'CO 管理会计',
    children: [
      ['KS01', '成本中心', '/co/cost-centers', () => import('../views/co/CostCenterList.vue')],
      ['KSB1', 'CO 凭证', '/co/documents', () => import('../views/co/CoDocumentList.vue')],
      ['S_ALR_87013611', 'CO 报表', '/co/report', () => import('../views/co/CoReport.vue')],
    ],
  },
  {
    name: 'PP 生产计划',
    children: [
      ['CS01', 'BOM', '/pp/bom', () => import('../views/pp/BomList.vue')],
      ['CO01', '创建生产订单', '/pp/orders', () => import('../views/pp/ProductionOrderList.vue')],
      ['CO02', '修改生产订单', '/pp/orders', () => import('../views/pp/ProductionOrderList.vue')],
      ['CO11N', '报工', '/pp/orders', () => import('../views/pp/ProductionOrderList.vue')],
      ['COOIS', '生产订单信息', '/pp/orders', () => import('../views/pp/ProductionOrderList.vue')],
    ],
  },
  {
    name: 'Basis 基础管理',
    children: [
      ['SU01', '用户管理', '/basis/users', () => import('../views/basis/UserList.vue')],
      ['SPRO', '组织结构', '/basis/org', () => import('../views/basis/OrgStructure.vue')],
      ['SE16', '事务码目录', '/basis/tcodes', () => import('../views/basis/TcodeList.vue')],
      ['SM37', '操作日志', '/basis/op-logs', () => import('../views/basis/OpLogList.vue')],
    ],
  },
  {
    name: '集成 Integration',
    children: [
      ['SLG1', '集成日志', '/integration/logs', () => import('../views/integration/IntegrationLogList.vue')],
      ['INTF', '接口文档', '/integration/docs', () => import('../views/integration/ApiDoc.vue')],
    ],
  },
]
