package com.travelinsurance.dto;

import com.travelinsurance.entity.PolicyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyResponse {
    private Long id;
    private String policyNumber;
    private Long applicationId;
    private String applicationNumber;
    private Long planId;
    private String planName;
    private String userName;
    private String userEmail;
    private BigDecimal coverageAmount;
    private BigDecimal premiumAmount;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private PolicyStatus status;
    private LocalDateTime createdAt;
    private String documentName;
    private String documentPath;
}
