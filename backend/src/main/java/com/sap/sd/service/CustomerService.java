package com.sap.sd.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sap.sd.entity.Customer;
import com.sap.sd.mapper.CustomerMapper;
import org.springframework.stereotype.Service;

@Service
public class CustomerService extends ServiceImpl<CustomerMapper, Customer> {
}
