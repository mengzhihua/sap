# SAP 复刻 ERP

## 项目简介

本项目是一个面向 SRM、BMS 集成场景的 SAP 风格 ERP 复刻系统，提供从主数据、采购、库存、销售、财务到生产的完整业务骨架，并保留与外部系统对接所需的接口契约。

系统模块包括：

- **Basis**：用户、角色、组织结构、事务代码、操作日志
- **MM**：物料、供应商、采购申请、采购订单、MIGO、库存、MIRO、GR/IR
- **SD**：客户、销售订单、交货、拣配、发货过账、开票
- **FI**：总账科目、FB50、财务凭证、付款/收款、清账、余额、科目确定
- **CO**：成本中心、CO 凭证、成本中心报表
- **PP**：BOM、生产订单、发料、报工、完工入库、技术完成
- **集成**：SRM OData 风格入站、BMS 出入站、集成日志
- **Dashboard**：采购、库存、收入、成本、未清项、生产状态和集成成功率汇总

## 技术栈

- JDK 8 源码级兼容
- Spring Boot 2.7.18
- MyBatis-Plus
- MySQL 8（默认数据库）
- H2（可选的本地文件数据库和测试数据库）
- Vue 3.5
- Vite 5
- Element Plus 2.9
- Vue Router 4
- Axios 1.7

## 目录结构

```text
sap/
├── backend/
│   ├── src/main/java/com/sap/
│   │   ├── basis/          # Basis
│   │   ├── mm/             # 物料管理
│   │   ├── sd/             # 销售与分销
│   │   ├── fi/             # 财务会计
│   │   ├── co/             # 管理会计
│   │   ├── pp/             # 生产计划
│   │   ├── integration/    # SRM/BMS 集成
│   │   ├── dashboard/      # Dashboard 汇总
│   │   ├── common/         # R、分页、通用异常等
│   │   └── system/         # 登录、认证、权限
│   └── src/main/resources/
│       ├── application.yml
│       ├── application-h2.yml
│       ├── schema.sql
│       └── data.sql
├── frontend/
│   ├── src/api/             # 按模块拆分的 API 客户端
│   ├── src/layout/          # SAP Fiori 风格布局
│   ├── src/router/          # 路由和 T-code 分组
│   └── src/views/           # 各业务模块页面
├── scripts/smoke.sh         # 后端全链路冒烟脚本
├── docker-compose.yml       # MySQL 8
└── .gitignore
```

## 快速启动

### 1. 启动 MySQL

在仓库根目录执行：

```bash
docker compose up -d
```

默认创建：

- 容器：`sap-mysql`
- 端口：`3306`
- 数据库：`sap`
- 用户：`root`
- 密码：`root`

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端默认监听 `http://localhost:8085`。默认 MySQL 连接配置可通过以下环境变量覆盖：

| 环境变量 | 默认值 |
|---|---|
| `DB_HOST` | `localhost` |
| `DB_PORT` | `3306` |
| `DB_NAME` | `sap` |
| `DB_USER` | `root` |
| `DB_PASSWORD` | `root` |

使用 H2 模式：

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=h2"
```

H2 使用 `./data/sap` 文件数据库，控制台地址为 `http://localhost:8085/h2`。

### 3. 启动前端

另开终端执行：

```bash
cd frontend
npm i
npm run dev
```

前端默认监听 `http://localhost:5175`，Vite 将 `/api` 代理到 `http://localhost:8085`。

### 默认账号

```text
用户名：admin
密码：admin123
```

管理员密码可通过 `SAP_ADMIN_PASSWORD` 覆盖。登录接口为：

```text
POST /api/auth/login
```

登录后前端使用 Bearer Token 调用受保护的业务接口。

## 事务代码表

事务代码由后端 `sap_tcode` 目录和前端路由共同维护。输入事务代码后，前端会查询 `/api/basis/tcodes?q=...` 并跳转到对应页面。

| 模块 | T-code | 页面 |
|---|---|---|
| MM | `MM01` | `/mm/materials` 物料主数据 |
| MM | `MM03` | `/mm/materials` 物料显示 |
| MM | `MK01` | `/mm/vendors` 供应商主数据 |
| MM | `ME51N` | `/mm/pr` 采购申请 |
| MM | `ME21N` | `/mm/po` 采购订单 |
| MM | `ME23N` | `/mm/po` 采购订单显示 |
| MM | `MIGO` | `/mm/migo` 货物移动 |
| MM | `MMBE` | `/mm/stock` 库存总览 |
| MM | `MIRO` | `/mm/miro` 发票校验 |
| MM | `MB51` | `/mm/material-docs` 物料凭证 |
| MM | `MIR4` | `/mm/invoices` 供应商发票 |
| MM | `GRIR` | `/mm/gr-ir` GR/IR |
| MM | `ME63` | `/mm/evaluations` 供应商评价 |
| SD | `VD01` | `/sd/customers` 客户主数据 |
| SD | `VA01` | `/sd/so` 销售订单 |
| SD | `VL01N` | `/sd/dn` 外向交货 |
| SD | `VF01` | `/sd/billing` 开票 |
| FI | `FS00` | `/fi/gl-accounts` 总账科目 |
| FI | `FB50` | `/fi/fb50` 总账凭证 |
| FI | `FB03` | `/fi/documents` 财务凭证 |
| FI | `F-53` | `/fi/payments` 付款 |
| FI | `F-28` | `/fi/payments` 收款 |
| FI | `FB08` | `/fi/documents` 冲销凭证 |
| FI | `FAGLB03` | `/fi/balances` 余额报表 |
| FI | `FBL1N` | `/fi/ap` 应付未清项 |
| FI | `FBL5N` | `/fi/ar` 应收未清项 |
| FI | `OBYC` | `/fi/determination` 科目确定 |
| CO | `KS01` | `/co/cost-centers` 成本中心 |
| CO | `KSB1` | `/co/documents` CO 凭证 |
| CO | `S_ALR_87013611` | `/co/report` CO 报表 |
| PP | `CS01` | `/pp/bom` BOM |
| PP | `CO01` | `/pp/orders` 创建生产订单 |
| PP | `CO02` | `/pp/orders` 修改生产订单 |
| PP | `CO11N` | `/pp/orders` 报工 |
| PP | `COOIS` | `/pp/orders` 生产订单信息 |
| Basis | `SU01` | `/basis/users` 用户管理 |
| Basis | `SPRO` | `/basis/org` 组织结构 |
| Basis | `SE16` | `/basis/tcodes` 事务代码目录 |
| Basis | `SM37` | `/basis/op-logs` 操作日志 |
| 集成 | `SLG1` | `/integration/logs` 集成日志 |
| 集成 | `INTF` | `/integration/docs` 接口文档 |

此外，首页和驾驶舱分别为 `/launchpad` 和 `/dashboard`。

## 与 SRM 的对接

SAP 提供 4 个无 `/api` 前缀的 OData 风格入站接口，使用 Basic Auth：

```text
用户名：srm
密码：srm123
```

接口路径：

```text
POST /API_PURCHASEORDER_PROCESS_SRV/A_PurchaseOrder
POST /API_MATERIAL_DOCUMENT_SRV/A_MaterialDocumentHeader
POST /API_SUPPLIERINVOICE_PROCESS_SRV/A_SupplierInvoice
POST /API_SUPPLIER_EVALUATION_SRV/A_SupplierEvaluation
```

成功响应保持 SAP 风格：

```json
{"d":{"PurchaseOrder":"4500000001"}}
{"d":{"MaterialDocument":"5000000001","MaterialDocumentYear":"2026"}}
{"d":{"SupplierInvoice":"5100000001","FiscalYear":"2026"}}
{"d":{"EvaluationId":"EV..."}}
```

SRM 端切换到 HTTP SAP 客户端时配置：

```text
SRM_SAP_MODE=http
SRM_SAP_BASE_URL=http://<sap-host>:8085
```

SRM 发送的外部别名会在 SAP 端解析：

| 外部编码 | SAP 编码 |
|---|---|
| `SUP01` | `100010` |
| `SUP02` | `100020` |
| `SUP03` | `100030` |
| `SKU001` | `M1001` |
| `SKU002` | `M1002` |
| `SKU003` | `M1003` |
| `SKU004` | `M1004` |
| `P001` | `1000` |
| `CUST-001` | `200010` |
| `CUST-002` | `200020` |
| `CUST-003` | `200030` |

SAP 端 SRM 配置项：

```text
SAP_SRM_USERNAME=srm
SAP_SRM_PASSWORD=srm123
```

## 与 BMS 的对接

### SAP 出站推送

将 `SAP_BMS_MODE` 设置为 `http` 后，SAP 使用 `HttpBmsClient`，请求带有：

```text
X-Api-Key: <SAP_BMS_API_KEY>
```

出站接口：

```text
POST {SAP_BMS_BASE_URL}/api/open/oms/docs
POST {SAP_BMS_BASE_URL}/api/open/wms/docs
```

- SD 发货过账（PGI）推送 OMS 出库单
- MM 采购收货（MIGO 101）推送 WMS 入库单
- 默认模式为 `mock`，无需外部 BMS 即可运行演示和测试

相关配置项与默认值：

| 配置项 | 环境变量 | 默认值 |
|---|---|---|
| `sap.bms.mode` | `SAP_BMS_MODE` | `mock` |
| `sap.bms.base-url` | `SAP_BMS_BASE_URL` | `http://localhost:8080` |
| `sap.bms.api-key` | `SAP_BMS_API_KEY` | `dev-open-key` |
| `sap.bms.customer-code` | `SAP_BMS_CUSTOMER_CODE` | `CUST-001` |

### BMS 入站对账

BMS 向 SAP 回写对账单：

```text
POST /api/open/bms/statements
GET  /api/open/bms/statements/{statementNo}
```

请求必须携带：

```text
X-Api-Key: sap-open-key
```

可通过 `SAP_OPEN_API_KEY` 覆盖默认密钥。对账请求包含 `statementNo`、`direction`、`partnerCode`、`amount`、`taxAmount`、`bizDate`、`remark`，其中 `statementNo` 用于幂等处理。

## 核心业务规则

- **MIGO 101**：采购订单收货，数量不能超过开放收货数量；增加库存、更新 PO 收货数量和 GR/IR，并生成平衡的 FI 凭证。
- **MIGO 201**：成本中心发料，库存减少，必须提供有效成本中心；生成 FI 凭证和 CO 凭证。
- **MIGO 261**：生产订单发料，库存减少，必须提供生产订单；更新生产订单实际成本并生成 FI 凭证。
- **MIGO 311**：同工厂库存地点之间转储，不生成 FI 凭证。
- **MIRO**：执行 PO、收货、发票三单匹配；发票数量不能超过 `GR - IR`，单价偏差超过 PO 单价的 ±5% 时保存为 `BLOCKED` 且不生成 FI 凭证。
- **FB50**：至少两行，借方和贷方合计必须相等；不平衡凭证不能过账。
- **F-53/F-28**：分别处理应付付款和应收收款；支持选择开放项清账，已清账项从 AP/AR 未清项中消失。
- **FB08**：生成反向凭证，已冲销凭证和冲销凭证本身不能再次冲销，冲销凭证类型为 `AB`。
- **所有过账流程**：在允许的业务流程中生成借贷平衡的 FI 凭证，并写入对应的业务单据和集成日志。

## 测试与冒烟

运行后端单元和集成测试：

```bash
cd backend
mvn test
```

MySQL 已启动且后端监听 `8085` 时，运行全链路冒烟：

```bash
./scripts/smoke.sh
```

冒烟脚本覆盖登录、PR 转 PO、SRM 采购/收货/发票/评价、MIGO、MIRO、BMS 对账、Dashboard、交货列表和集成日志查询。
