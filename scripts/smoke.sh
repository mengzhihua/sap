#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8085}"
SAP_BASIC_AUTH="${SAP_BASIC_AUTH:-srm:srm123}"
OPEN_KEY="${OPEN_KEY:-sap-open-key}"

token="$(curl -fsS -X POST "$BASE_URL/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.data.token')"

po="$(curl -fsS -X POST "$BASE_URL/API_PURCHASEORDER_PROCESS_SRV/A_PurchaseOrder" \
  -u "$SAP_BASIC_AUTH" -H 'Content-Type: application/json' \
  -d '{"PurchaseOrderType":"NB","Supplier":"SUP01","PurchasingOrganization":"1000","PurchasingGroup":"001","CompanyCode":"1000","DocumentCurrency":"CNY","to_PurchaseOrderItem":[{"Material":"SKU001","OrderQuantity":2,"NetPriceAmount":45,"Plant":"P001"}]}')"
po_no="$(jq -r '.d.PurchaseOrder' <<<"$po")"

curl -fsS -X POST "$BASE_URL/API_MATERIAL_DOCUMENT_SRV/A_MaterialDocumentHeader" \
  -u "$SAP_BASIC_AUTH" -H 'Content-Type: application/json' \
  -d "{\"GoodsMovementCode\":\"01\",\"PostingDate\":\"$(date +%F)\",\"to_MaterialDocumentItem\":[{\"PurchaseOrder\":\"$po_no\",\"Material\":\"SKU001\",\"QuantityInBaseUnit\":2,\"GoodsMovementType\":\"101\",\"Plant\":\"P001\"}]}" >/dev/null

curl -fsS -X POST "$BASE_URL/API_SUPPLIERINVOICE_PROCESS_SRV/A_SupplierInvoice" \
  -u "$SAP_BASIC_AUTH" -H 'Content-Type: application/json' \
  -d "{\"Supplier\":\"SUP01\",\"PurchaseOrder\":\"$po_no\",\"InvoiceGrossAmount\":101.70,\"to_SuplrInvcItemPurOrdRef\":[{\"PurchaseOrderItem\":\"10\",\"QuantityInPurchaseOrderUnit\":2,\"PurchaseOrderItemPrice\":45}]}" >/dev/null

curl -fsS -X POST "$BASE_URL/API_SUPPLIER_EVALUATION_SRV/A_SupplierEvaluation" \
  -u "$SAP_BASIC_AUTH" -H 'Content-Type: application/json' \
  -d '{"Supplier":"SUP01","EvaluationPeriod":"2026-Q1","Score":92,"Grade":"A"}' >/dev/null

curl -fsS -X POST "$BASE_URL/api/open/bms/statements" \
  -H "X-Api-Key: $OPEN_KEY" -H 'Content-Type: application/json' \
  -d "{\"statementNo\":\"SMOKE-$(date +%s)\",\"direction\":\"AR\",\"partnerCode\":\"CUST-001\",\"amount\":113,\"taxAmount\":13,\"bizDate\":\"$(date +%F)\"}" >/dev/null

curl -fsS "$BASE_URL/api/dashboard/summary" \
  -H "Authorization: Bearer $token" >/dev/null

echo "SAP smoke flow passed"
