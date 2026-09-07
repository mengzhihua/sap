package com.sap.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SrmEvaluationPayload {
    @JsonProperty("Supplier") private String supplier;
    @JsonProperty("EvaluationPeriod") private String evaluationPeriod;
    @JsonProperty("Score") private BigDecimal score;
    @JsonProperty("Grade") private String grade;
}
