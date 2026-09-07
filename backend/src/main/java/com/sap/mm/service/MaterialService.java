package com.sap.mm.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sap.mm.entity.Material;
import com.sap.mm.mapper.MaterialMapper;
import org.springframework.stereotype.Service;

@Service
public class MaterialService extends ServiceImpl<MaterialMapper, Material> {
}
