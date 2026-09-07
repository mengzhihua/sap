package com.sap.co.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sap.co.entity.CoDocument;
import com.sap.co.entity.CostCenter;
import com.sap.co.mapper.CoDocumentMapper;
import com.sap.co.service.CostCenterService;
import com.sap.common.R;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/co")
public class CoController {
    private final CostCenterService costCenters;
    private final CoDocumentMapper documents;
    private final JdbcTemplate jdbc;

    public CoController(CostCenterService costCenters, CoDocumentMapper documents, JdbcTemplate jdbc) {
        this.costCenters = costCenters;
        this.documents = documents;
        this.jdbc = jdbc;
    }

    @GetMapping("/cost-centers")
    public R<List<CostCenter>> centers() {
        return R.ok(costCenters.list());
    }

    @PostMapping("/cost-centers")
    public R<CostCenter> createCenter(@Valid @RequestBody CostCenter center) {
        costCenters.save(center);
        return R.ok(center);
    }

    @PutMapping("/cost-centers/{id}")
    public R<CostCenter> updateCenter(@PathVariable String id, @RequestBody CostCenter input) {
        input.setKostl(id);
        costCenters.updateById(input);
        return R.ok(costCenters.getById(id));
    }

    @GetMapping("/documents")
    public R<List<CoDocument>> docs(@RequestParam(required = false) String kostl) {
        LambdaQueryWrapper<CoDocument> query = new LambdaQueryWrapper<>();
        if (kostl != null) {
            query.eq(CoDocument::getKostl, kostl);
        }
        return R.ok(documents.selectList(query));
    }

    @GetMapping("/report")
    public R<List<Map<String, Object>>> report() {
        return R.ok(jdbc.queryForList("SELECT kostl,cost_element,SUM(amount) amount " +
                "FROM sap_co_document GROUP BY kostl,cost_element"));
    }
}
