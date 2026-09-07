package com.sap.fi.dto;

import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

@Data
public class FiDocumentRequest {
    private String blart = "SA";
    private String source = "FI";
    @NotEmpty @Valid private List<Item> items;
    @Data public static class Item {
        private String saknr;
        private String shkzg;
        private BigDecimal amount;
        private String kostl;
        private String lifnr;
        private String kunnr;
        private String text;
    }
}
