package com.sap.basis.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sap.basis.dto.OrgRequest;
import com.sap.basis.dto.UserRequest;
import com.sap.basis.entity.*;
import com.sap.basis.mapper.*;
import com.sap.basis.service.TcodeService;
import com.sap.basis.service.UserService;
import com.sap.common.BizException;
import com.sap.common.PageResult;
import com.sap.common.R;
import com.sap.system.auth.PasswordHasher;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/basis")
public class BasisController {
    private final UserService users;
    private final CompanyCodeMapper companyCodes;
    private final PlantMapper plants;
    private final StorageLocationMapper storageLocations;
    private final PurchasingOrgMapper purchasingOrgs;
    private final PurchasingGroupMapper purchasingGroups;
    private final SalesOrgMapper salesOrgs;
    private final TcodeService tcodes;
    private final OpLogMapper opLogs;

    public BasisController(UserService users, CompanyCodeMapper companyCodes, PlantMapper plants,
                           StorageLocationMapper storageLocations, PurchasingOrgMapper purchasingOrgs,
                           PurchasingGroupMapper purchasingGroups, SalesOrgMapper salesOrgs,
                           TcodeService tcodes, OpLogMapper opLogs) {
        this.users = users;
        this.companyCodes = companyCodes;
        this.plants = plants;
        this.storageLocations = storageLocations;
        this.purchasingOrgs = purchasingOrgs;
        this.purchasingGroups = purchasingGroups;
        this.salesOrgs = salesOrgs;
        this.tcodes = tcodes;
        this.opLogs = opLogs;
    }

    @GetMapping("/users")
    public R<List<User>> users() {
        return R.ok(users.list());
    }

    @PostMapping("/users")
    public R<User> createUser(@Valid @RequestBody UserRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()
                || request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new BizException("用户名和密码不能为空");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(PasswordHasher.hash(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setRole(request.getRole() == null ? "MM" : request.getRole());
        user.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        users.save(user);
        return R.ok(user);
    }

    @PutMapping("/users/{id}")
    public R<User> updateUser(@PathVariable Long id, @RequestBody UserRequest request) {
        User user = users.getById(id);
        if (user == null) throw new BizException("用户不存在: " + id);
        if (request.getRealName() != null) user.setRealName(request.getRealName());
        if (request.getRole() != null) user.setRole(request.getRole());
        if (request.getStatus() != null) user.setStatus(request.getStatus());
        users.updateById(user);
        return R.ok(user);
    }

    @PutMapping("/users/{id}/password")
    public R<User> resetPassword(@PathVariable Long id, @RequestBody UserRequest request) {
        User user = users.getById(id);
        if (user == null) throw new BizException("用户不存在: " + id);
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new BizException("新密码不能为空");
        }
        user.setPassword(PasswordHasher.hash(request.getPassword()));
        users.updateById(user);
        return R.ok(user);
    }

    @DeleteMapping("/users/{id}")
    public R<Void> deleteUser(@PathVariable Long id) {
        if (!users.removeById(id)) throw new BizException("用户不存在: " + id);
        return R.ok(null);
    }

    @GetMapping("/roles")
    public R<List<Object>> roles() {
        return R.ok(Arrays.<Object>asList(role("ADMIN", "管理员"), role("MM", "物料管理"),
                role("SD", "销售管理"), role("FI", "财务管理"), role("CO", "成本管理"),
                role("PP", "生产管理")));
    }

    @GetMapping("/org/{kind}")
    public R<?> org(@PathVariable String kind) {
        return R.ok(orgList(kind));
    }

    @PostMapping("/org/{kind}")
    public R<?> createOrg(@PathVariable String kind, @Valid @RequestBody OrgRequest request) {
        Object result = orgEntity(kind, request);
        if (result instanceof CompanyCode) companyCodes.insert((CompanyCode) result);
        else if (result instanceof Plant) plants.insert((Plant) result);
        else if (result instanceof StorageLocation) storageLocations.insert((StorageLocation) result);
        else if (result instanceof PurchasingOrg) purchasingOrgs.insert((PurchasingOrg) result);
        else if (result instanceof PurchasingGroup) purchasingGroups.insert((PurchasingGroup) result);
        else if (result instanceof SalesOrg) salesOrgs.insert((SalesOrg) result);
        return R.ok(result);
    }

    @GetMapping("/tcodes")
    public R<List<Tcode>> tcodes(@RequestParam(required = false) String q) {
        LambdaQueryWrapper<Tcode> query = new LambdaQueryWrapper<>();
        if (q != null && !q.trim().isEmpty()) {
            query.like(Tcode::getTcode, q).or().like(Tcode::getName, q);
        }
        return R.ok(tcodes.list(query));
    }

    @GetMapping("/op-logs")
    public R<PageResult<OpLog>> logs(@RequestParam(defaultValue = "1") long page,
                                     @RequestParam(defaultValue = "20") long size) {
        List<OpLog> all = opLogs.selectList(new LambdaQueryWrapper<OpLog>().orderByDesc(OpLog::getId));
        return R.ok(page(all, page, size));
    }

    private Object orgList(String kind) {
        if ("company-codes".equals(kind)) return companyCodes.selectList(null);
        if ("plants".equals(kind)) return plants.selectList(null);
        if ("storage-locations".equals(kind)) return storageLocations.selectList(null);
        if ("purchasing-orgs".equals(kind)) return purchasingOrgs.selectList(null);
        if ("purchasing-groups".equals(kind)) return purchasingGroups.selectList(null);
        if ("sales-orgs".equals(kind)) return salesOrgs.selectList(null);
        throw new BizException("未知组织类型: " + kind);
    }

    private Object orgEntity(String kind, OrgRequest request) {
        if ("company-codes".equals(kind)) {
            CompanyCode value = new CompanyCode();
            value.setBukrs(request.getCode());
            value.setName(request.getName());
            value.setCurrency(request.getCurrency());
            return value;
        }
        if ("plants".equals(kind)) {
            Plant value = new Plant();
            value.setWerks(request.getCode());
            value.setName(request.getName());
            value.setBukrs(request.getBukrs());
            value.setBmsWarehouseCode(request.getBmsWarehouseCode());
            return value;
        }
        if ("storage-locations".equals(kind)) {
            StorageLocation value = new StorageLocation();
            value.setWerks(request.getWerks());
            value.setLgort(request.getLgort());
            value.setName(request.getName());
            return value;
        }
        if ("purchasing-orgs".equals(kind)) {
            PurchasingOrg value = new PurchasingOrg();
            value.setEkorg(request.getCode());
            value.setName(request.getName());
            return value;
        }
        if ("purchasing-groups".equals(kind)) {
            PurchasingGroup value = new PurchasingGroup();
            value.setEkgrp(request.getCode());
            value.setName(request.getName());
            return value;
        }
        if ("sales-orgs".equals(kind)) {
            SalesOrg value = new SalesOrg();
            value.setVkorg(request.getCode());
            value.setName(request.getName());
            value.setBukrs(request.getBukrs());
            return value;
        }
        throw new BizException("未知组织类型: " + kind);
    }

    private static Object role(String code, String name) {
        java.util.Map<String, String> result = new java.util.LinkedHashMap<>();
        result.put("code", code);
        result.put("name", name);
        return result;
    }

    private static <T> PageResult<T> page(List<T> all, long page, long size) {
        long from = Math.max(0, (page - 1) * size);
        long to = Math.min(all.size(), from + size);
        List<T> records = from >= all.size() ? java.util.Collections.<T>emptyList()
                : all.subList((int) from, (int) to);
        return new PageResult<>(all.size(), page, size, records);
    }
}
