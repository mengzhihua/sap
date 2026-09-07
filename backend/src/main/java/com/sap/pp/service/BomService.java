package com.sap.pp.service;
import com.sap.mm.service.ReferenceDataService;
import com.sap.pp.dto.BomRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Service
public class BomService {
    private final JdbcTemplate jdbc; private final ReferenceDataService refs;
    public BomService(JdbcTemplate jdbc,ReferenceDataService refs){this.jdbc=jdbc;this.refs=refs;}
    @Transactional public Map<String,Object> create(BomRequest request){
        String mat=refs.material(request.getMatnr()), werks=refs.plant(request.getWerks());
        jdbc.update("DELETE FROM sap_bom_item WHERE matnr=? AND werks=?",mat,werks);
        jdbc.update("DELETE FROM sap_bom WHERE matnr=? AND werks=?",mat,werks);
        jdbc.update("INSERT INTO sap_bom(matnr,werks,base_qty) VALUES(?,?,?)",mat,werks,request.getBaseQty()==null?BigDecimal.ONE:request.getBaseQty());
        for(BomRequest.Item item:request.getItems())
            jdbc.update("INSERT INTO sap_bom_item(matnr,werks,component,qty) VALUES(?,?,?,?)",mat,werks,
                    refs.material(item.getComponent()==null?item.getMatnr():item.getComponent()),item.getQty());
        return jdbc.queryForMap("SELECT * FROM sap_bom WHERE matnr=? AND werks=?",mat,werks);
    }
}
