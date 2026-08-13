package com.travelinsurance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsurancePlanRequest {

    @NotBlank(message = "Plan name is required")
    @Size(max = 100, message = "Plan name must be less than 100 characters")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Coverage amount is required")
    @DecimalMin(value = "0.0", message = "Coverage amount must be greater than or equal to zero")
    private BigDecimal coverageAmount;

    @NotNull(message = "Medical coverage is required")
    @DecimalMin(value = "0.0", message = "Medical coverage must be greater than or equal to zero")
    private BigDecimal medicalCoverage;

    @NotNull(message = "Baggage coverage is required")
    @DecimalMin(value = "0.0", message = "Baggage coverage must be greater than or equal to zero")
    private BigDecimal baggageCoverage;

    @NotNull(message = "Trip cancellation is required")
    @DecimalMin(value = "0.0", message = "Trip cancellation must be greater than or equal to zero")
    private BigDecimal tripCancellation;

    private Boolean emergencyAssistance;

    @NotNull(message = "Base premium is required")
    @DecimalMin(value = "0.01", message = "Base premium must be greater than zero")
    private BigDecimal basePremium;

    private Boolean active;
}
