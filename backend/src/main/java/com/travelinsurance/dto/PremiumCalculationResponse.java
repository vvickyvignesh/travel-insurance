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
public class PremiumCalculationResponse {
    private Long applicationId;
    private String applicationNumber;
    private String planName;
    private BigDecimal basePremium;
    private Long tripDuration;
    private BigDecimal durationMultiplier;
    private Integer travellerAge;
    private BigDecimal ageMultiplier;
    private String destination;
    private BigDecimal destinationMultiplier;
    private String tripType;
    private BigDecimal tripTypeMultiplier;
    private BigDecimal finalPremium;
    private String currency;
    private LocalDateTime calculatedAt;
}
