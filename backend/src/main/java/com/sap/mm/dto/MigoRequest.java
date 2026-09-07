package com.sap.mm.dto;

import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

@Data
public class MigoRequest {
    private String bwart = "101";
    private String refType;
    private String refNo;
    private String aufnr;
    private String vbeln;
    @NotEmpty @Valid private List<MigoItemRequest> items;
    @Data public static class MigoItemRequest {
        private String matnr;
        private String werks = "1000";
        private String lgort = "0001";
        private String toLgort;
        private String ebelp;
        private BigDecimal qty;
        private BigDecimal quantityInBaseUnit;
        private String kostl;
    }
}
