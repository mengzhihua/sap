package com.sap.basis.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sap.basis.entity.CompanyCode;
import com.sap.basis.mapper.CompanyCodeMapper;
import org.springframework.stereotype.Service;

@Service
public class OrgService extends ServiceImpl<CompanyCodeMapper, CompanyCode> {
}
