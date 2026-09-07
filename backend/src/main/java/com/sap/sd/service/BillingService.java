package com.sap.sd.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sap.common.BizException;
import com.sap.common.NumberRangeService;
import com.sap.fi.service.AccountingDocumentService;
import com.sap.mm.entity.Material;
import com.sap.mm.mapper.MaterialMapper;
import com.sap.sd.dto.BillingRequest;
import com.sap.sd.entity.BillingDoc;
import com.sap.sd.entity.BillingDocItem;
import com.sap.sd.entity.Delivery;
import com.sap.sd.entity.DeliveryItem;
import com.sap.sd.mapper.BillingDocItemMapper;
import com.sap.sd.mapper.BillingDocMapper;
import com.sap.sd.mapper.DeliveryItemMapper;
import com.sap.sd.mapper.DeliveryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

@Service
public class BillingService {
    private final BillingDocMapper billings;
    private final BillingDocItemMapper billingItems;
    private final DeliveryMapper deliveries;
    private final DeliveryItemMapper deliveryItems;
    private final MaterialMapper materials;
    private final NumberRangeService numbers;
    private final AccountingDocumentService accounting;

    public BillingService(BillingDocMapper billings, BillingDocItemMapper billingItems, DeliveryMapper deliveries,
                          DeliveryItemMapper deliveryItems, MaterialMapper materials, NumberRangeService numbers,
                          AccountingDocumentService accounting) {
        this.billings = billings; this.billingItems = billingItems; this.deliveries = deliveries;
        this.deliveryItems = deliveryItems; this.materials = materials; this.numbers = numbers; this.accounting = accounting;
    }

    @Transactional
    public BillingDoc create(BillingRequest request) {
        Delivery delivery = deliveries.selectById(request.getDnVbeln());
        if (delivery == null) throw new BizException("交货单不存在: " + request.getDnVbeln());
        BigDecimal net = BigDecimal.ZERO;
        for (DeliveryItem line : deliveryItems.selectList(new LambdaQueryWrapper<DeliveryItem>()
                .eq(DeliveryItem::getVbeln, delivery.getVbeln()))) {
            Material material = materials.selectById(line.getMatnr());
            net = net.add(zero(line.getQty()).multiply(zero(material == null ? null : material.getStdPrice())));
        }
        BigDecimal tax = net.multiply(new BigDecimal("0.13")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal gross = net.add(tax);
        String vbeln = numbers.next("BILLING");
        String fi = accounting.post("DR", "SD", vbeln, Arrays.asList(
                new AccountingDocumentService.FiLine("1122", "S", gross, null, null, delivery.getKunnr(), "应收"),
                new AccountingDocumentService.FiLine("6001", "H", net, null, null, null, "收入"),
                new AccountingDocumentService.FiLine("2221", "H", tax, null, null, null, "销项税")));
        BillingDoc billing = new BillingDoc();
        billing.setVbeln(vbeln); billing.setFkart("F2"); billing.setKunnr(delivery.getKunnr());
        billing.setNet(net); billing.setTax(tax); billing.setGross(gross); billing.setFiBelnr(fi); billing.setStatus("POSTED");
        billings.insert(billing);
        billing.setItems(java.util.Collections.emptyList());
        return billing;
    }

    private static BigDecimal zero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
}
