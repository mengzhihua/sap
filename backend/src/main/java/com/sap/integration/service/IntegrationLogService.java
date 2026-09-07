package com.sap.integration.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sap.integration.entity.IntegrationLog;
import com.sap.integration.mapper.IntegrationLogMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IntegrationLogService {
    private final IntegrationLogMapper logs;

    public IntegrationLogService(IntegrationLogMapper logs) {
        this.logs = logs;
    }

    public void write(String direction, String system, String action, Object request,
                      Object response, boolean success, String error, long elapsed) {
        IntegrationLog log = new IntegrationLog();
        log.setDirection(direction); log.setSystemName(system); log.setActionName(action);
        log.setRequest(String.valueOf(request)); log.setResponse(String.valueOf(response));
        log.setSuccess(success ? 1 : 0); log.setError(error); log.setElapsedMs(elapsed);
        logs.insert(log);
    }

    public List<IntegrationLog> list(String system, String direction) {
        LambdaQueryWrapper<IntegrationLog> query = new LambdaQueryWrapper<>();
        if (system != null) query.eq(IntegrationLog::getSystemName, system);
        if (direction != null) query.eq(IntegrationLog::getDirection, direction);
        return logs.selectList(query);
    }
}
