package com.sap.pp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sap.mm.service.ReferenceDataService;
import com.sap.pp.dto.BomRequest;
import com.sap.pp.entity.Bom;
import com.sap.pp.entity.BomItem;
import com.sap.pp.mapper.BomItemMapper;
import com.sap.pp.mapper.BomMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class BomService {
    private final BomMapper boms;
    private final BomItemMapper items;
    private final ReferenceDataService refs;

    public BomService(BomMapper boms, BomItemMapper items, ReferenceDataService refs) {
        this.boms = boms; this.items = items; this.refs = refs;
    }

    @Transactional
    public Bom create(BomRequest request) {
        String matnr = refs.material(request.getMatnr());
        String werks = refs.plant(request.getWerks());
        items.delete(new LambdaQueryWrapper<BomItem>().eq(BomItem::getMatnr, matnr).eq(BomItem::getWerks, werks));
        Bom bom = boms.selectOne(new LambdaQueryWrapper<Bom>().eq(Bom::getMatnr, matnr).eq(Bom::getWerks, werks));
        if (bom == null) {
            bom = new Bom(); bom.setMatnr(matnr); bom.setWerks(werks); boms.insert(bom);
        }
        bom.setBaseQty(request.getBaseQty() == null ? BigDecimal.ONE : request.getBaseQty());
        boms.update(bom, new LambdaQueryWrapper<Bom>().eq(Bom::getMatnr, matnr).eq(Bom::getWerks, werks));
        for (BomRequest.Item item : request.getItems()) {
            BomItem line = new BomItem();
            line.setMatnr(matnr); line.setWerks(werks);
            line.setComponent(refs.material(item.getComponent() == null ? item.getMatnr() : item.getComponent()));
            line.setQty(item.getQty()); items.insert(line);
        }
        return find(matnr, werks);
    }

    public Bom find(String matnr, String werks) {
        Bom bom = boms.selectOne(new LambdaQueryWrapper<Bom>().eq(Bom::getMatnr, matnr).eq(Bom::getWerks, werks));
        if (bom != null) bom.setBaseQty(bom.getBaseQty());
        return bom;
    }
}
