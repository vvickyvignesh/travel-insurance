package com.travelinsurance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsurancePlanResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal coverageAmount;
    private BigDecimal medicalCoverage;
    private BigDecimal baggageCoverage;
    private BigDecimal tripCancellation;
    private Boolean emergencyAssistance;
    private BigDecimal basePremium;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
