package com.sap.mm.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sap.mm.entity.Vendor;
import com.sap.mm.mapper.VendorMapper;
import org.springframework.stereotype.Service;

@Service
public class VendorService extends ServiceImpl<VendorMapper, Vendor> {
}
