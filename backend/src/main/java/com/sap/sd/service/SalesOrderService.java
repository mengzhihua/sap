package com.sap.sd.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sap.common.NumberRangeService;
import com.sap.mm.entity.Material;
import com.sap.mm.mapper.MaterialMapper;
import com.sap.mm.service.ReferenceDataService;
import com.sap.sd.dto.SoCreateRequest;
import com.sap.sd.entity.SalesOrder;
import com.sap.sd.entity.SalesOrderItem;
import com.sap.sd.mapper.SalesOrderItemMapper;
import com.sap.sd.mapper.SalesOrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SalesOrderService {
    private final SalesOrderMapper orders;
    private final SalesOrderItemMapper items;
    private final MaterialMapper materials;
    private final NumberRangeService numbers;
    private final ReferenceDataService refs;

    public SalesOrderService(SalesOrderMapper orders, SalesOrderItemMapper items, MaterialMapper materials,
                             NumberRangeService numbers, ReferenceDataService refs) {
        this.orders = orders; this.items = items; this.materials = materials;
        this.numbers = numbers; this.refs = refs;
    }

    @Transactional
    public SalesOrder create(SoCreateRequest request) {
        String vbeln = numbers.next("SO");
        SalesOrder order = new SalesOrder();
        order.setVbeln(vbeln); order.setAuart("OR");
        order.setKunnr(refs.customer(request.getKunnr() == null ? request.getCustomerCode() : request.getKunnr()));
        order.setVkorg(value(request.getVkorg(), "1000")); order.setWaers(value(request.getWaers(), "CNY"));
        order.setStatus("OPEN"); orders.insert(order);
        int pos = 10;
        for (SoCreateRequest.Item requestItem : request.getItems()) {
            String matnr = refs.material(requestItem.getMatnr());
            Material material = materials.selectById(matnr);
            SalesOrderItem item = new SalesOrderItem();
            item.setVbeln(vbeln); item.setPosnr(String.valueOf(pos)); item.setMatnr(matnr);
            item.setWerks(value(requestItem.getWerks(), "1000")); item.setKwmeng(requestItem.getQty());
            item.setNetpr(requestItem.getPrice() == null ? material.getStdPrice() : requestItem.getPrice());
            item.setDeliveredQty(BigDecimal.ZERO); item.setBilledQty(BigDecimal.ZERO); items.insert(item); pos += 10;
        }
        return load(order);
    }

    public List<SalesOrder> list() {
        List<SalesOrder> result = orders.selectList(null);
        result.forEach(this::load);
        return result;
    }

    public SalesOrder one(String id) { return load(orders.selectById(id)); }

    private SalesOrder load(SalesOrder order) {
        if (order != null) order.setItems(items.selectList(new LambdaQueryWrapper<SalesOrderItem>()
                .eq(SalesOrderItem::getVbeln, order.getVbeln())));
        return order;
    }

    private static String value(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
