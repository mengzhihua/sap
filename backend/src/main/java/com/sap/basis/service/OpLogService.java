package com.sap.basis.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sap.basis.entity.OpLog;
import com.sap.basis.mapper.OpLogMapper;
import org.springframework.stereotype.Service;

@Service
public class OpLogService extends ServiceImpl<OpLogMapper, OpLog> {
}
