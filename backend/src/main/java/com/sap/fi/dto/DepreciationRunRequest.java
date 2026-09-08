package com.sap.fi.dto;

import lombok.Data;
import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class DepreciationRunRequest {
    @NotBlank @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])") private String period;
    private LocalDate postingDate;
}
