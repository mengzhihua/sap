package com.sap.flow;

import com.sap.TestSupport;
import com.sap.fi.dto.AssetAcquisitionRequest;
import com.sap.fi.dto.DepreciationRunRequest;
import com.sap.fi.dto.FixedAssetRequest;
import com.sap.fi.entity.AssetTransaction;
import com.sap.fi.entity.FixedAsset;
import com.sap.fi.service.FixedAssetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FixedAssetTest extends TestSupport {
    @Autowired private FixedAssetService service;

    @Test
    void acquisitionAndDepreciationUpdateAssetLedgerAndRemainIdempotent() {
        FixedAssetRequest master = new FixedAssetRequest();
        master.setName("测试设备");
        master.setAssetClass("MACHINE");
        master.setBukrs("1000");
        master.setKostl("CC1000");
        master.setUsefulLifeMonths(10);
        master.setSalvageValue(new BigDecimal("100.00"));
        FixedAsset asset = service.create(master);

        AssetAcquisitionRequest acquisition = new AssetAcquisitionRequest();
        acquisition.setAmount(new BigDecimal("1100.00"));
        acquisition.setPostingDate(LocalDate.now());
        AssetTransaction transaction = service.acquire(asset.getAnln1(), acquisition);
        assertNotNull(transaction.getBelnr());
        assertEquals("ACTIVE", service.requireAsset(asset.getAnln1()).getStatus());

        DepreciationRunRequest run = new DepreciationRunRequest();
        run.setPeriod(YearMonth.now().toString());
        run.setPostingDate(LocalDate.now());
        Map<String, Object> first = service.depreciate(run);
        Map<String, Object> repeated = service.depreciate(run);

        assertEquals(1, first.get("postedAssets"));
        assertEquals(new BigDecimal("100.00"), first.get("amount"));
        assertEquals(0, repeated.get("postedAssets"));
        assertEquals(new BigDecimal("1000.00"), service.requireAsset(asset.getAnln1()).getBookValue());
    }
}
