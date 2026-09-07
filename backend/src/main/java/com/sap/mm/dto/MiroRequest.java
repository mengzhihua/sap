package com.sap.mm.dto;

import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

@Data
public class MiroRequest {
    @NotBlank private String lifnr;
    private String ebeln;
    private String externalInvoiceNo;
    private BigDecimal grossAmount;
    @NotEmpty @Valid private List<MiroItemRequest> items;
    @Data public static class MiroItemRequest {
        private String ebeln;
        private String ebelp;
        private BigDecimal qty;
        private BigDecimal price;
    }
}
