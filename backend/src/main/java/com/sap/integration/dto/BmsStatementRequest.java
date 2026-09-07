package com.sap.integration.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class BmsStatementRequest {
    @NotBlank private String statementNo;
    @NotBlank private String direction;
    @NotBlank private String partnerCode;
    @NotNull private BigDecimal amount;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private String bizDate;
    private String remark;
}
