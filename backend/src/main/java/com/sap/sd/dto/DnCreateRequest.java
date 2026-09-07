package com.sap.sd.dto;

import lombok.Data;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DnCreateRequest {
    private String soVbeln;
    private String werks = "1000";
    @Valid private List<Item> items;
    @Data public static class Item {
        private BigDecimal qty;
    }
}
