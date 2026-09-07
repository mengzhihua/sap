package com.sap.mm.service;

import com.sap.common.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sap.basis.entity.Plant;
import com.sap.basis.mapper.PlantMapper;
import com.sap.mm.entity.Material;
import com.sap.mm.entity.Vendor;
import com.sap.mm.mapper.MaterialMapper;
import com.sap.mm.mapper.VendorMapper;
import com.sap.sd.entity.Customer;
import com.sap.sd.mapper.CustomerMapper;
import org.springframework.stereotype.Service;

@Service
public class ReferenceDataService {
    private final VendorMapper vendors;
    private final MaterialMapper materials;
    private final PlantMapper plants;
    private final CustomerMapper customers;

    public ReferenceDataService(VendorMapper vendors, MaterialMapper materials,
                                PlantMapper plants, CustomerMapper customers) {
        this.vendors = vendors;
        this.materials = materials;
        this.plants = plants;
        this.customers = customers;
    }

    public String vendor(String value) {
        if (value == null || value.trim().isEmpty()) throw new BizException("供应商不能为空");
        Vendor vendor = vendors.selectOne(new LambdaQueryWrapper<Vendor>()
                .eq(Vendor::getLifnr, value).or().eq(Vendor::getAliasCode, value));
        if (vendor == null) throw new BizException("供应商不存在: " + value);
        return vendor.getLifnr();
    }
    public String material(String value) {
        if (value == null || value.trim().isEmpty()) throw new BizException("物料不能为空");
        Material material = materials.selectOne(new LambdaQueryWrapper<Material>()
                .eq(Material::getMatnr, value).or().eq(Material::getAliasCode, value));
        if (material == null) throw new BizException("物料不存在: " + value);
        return material.getMatnr();
    }
    public String maybeMaterial(String value) {
        if (value == null) return null;
        Material material = materials.selectOne(new LambdaQueryWrapper<Material>()
                .eq(Material::getMatnr, value).or().eq(Material::getAliasCode, value));
        return material == null ? value : material.getMatnr();
    }
    public String plant(String value) {
        if (value == null || value.trim().isEmpty()) return "1000";
        Plant plant = plants.selectById(value);
        if (plant == null && "P001".equals(value)) return "1000";
        if (plant == null) throw new BizException("工厂不存在: " + value);
        return plant.getWerks();
    }
    public String customer(String value) {
        if (value == null || value.trim().isEmpty()) throw new BizException("客户不能为空");
        Customer customer = customers.selectOne(new LambdaQueryWrapper<Customer>()
                .eq(Customer::getKunnr, value).or().eq(Customer::getAliasCode, value));
        if (customer == null) throw new BizException("客户不存在: " + value);
        return customer.getKunnr();
    }
    public String alias(String table, String key, String value) {
        if ("sap_customer".equals(table)) {
            Customer customer = customers.selectById(value);
            return customer == null || customer.getAliasCode() == null ? value : customer.getAliasCode();
        }
        if ("sap_vendor".equals(table)) {
            Vendor vendor = vendors.selectById(value);
            return vendor == null || vendor.getAliasCode() == null ? value : vendor.getAliasCode();
        }
        return value;
    }
    public String stockAccount(String matnr) {
        Material material = materials.selectById(matnr);
        return material != null && "ROH".equals(material.getMtart()) ? "1411" : "1405";
    }
    public String warehouse(String werks) {
        Plant plant = plants.selectById(werks);
        return plant == null || plant.getBmsWarehouseCode() == null ? werks : plant.getBmsWarehouseCode();
    }
}
