package com.sap.fi.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sap.fi.entity.Payment;
import com.sap.fi.mapper.PaymentMapper;
import org.springframework.stereotype.Service;

@Service
public class PaymentService extends ServiceImpl<PaymentMapper, Payment> {
}
