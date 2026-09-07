package com.sap.integration.controller;

import com.sap.common.BizException;
import com.sap.common.PageResult;
import com.sap.common.R;
import com.sap.integration.entity.IntegrationLog;
import com.sap.integration.service.IntegrationLogService;
import com.sap.sd.entity.Delivery;
import com.sap.sd.service.DeliveryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/integration")
public class IntegrationController {
    private final IntegrationLogService logs;
    private final DeliveryService deliveries;

    public IntegrationController(IntegrationLogService logs, DeliveryService deliveries) {
        this.logs = logs;
        this.deliveries = deliveries;
    }

    @GetMapping("/logs")
    public R<PageResult<IntegrationLog>> logs(@RequestParam(required = false) String system,
                                              @RequestParam(required = false) String direction,
                                              @RequestParam(defaultValue = "1") long page,
                                              @RequestParam(defaultValue = "20") long size) {
        return R.ok(logs.page(system, direction, page, size));
    }

    @GetMapping("/logs/{id}")
    public R<IntegrationLog> log(@PathVariable Long id) {
        IntegrationLog result = logs.one(id);
        if (result == null) throw new BizException("集成日志不存在: " + id);
        return R.ok(result);
    }

    @PostMapping("/bms/deliveries/{dnVbeln}/push")
    public R<Delivery> push(@PathVariable String dnVbeln) {
        return R.ok(deliveries.repush(dnVbeln));
    }
}
