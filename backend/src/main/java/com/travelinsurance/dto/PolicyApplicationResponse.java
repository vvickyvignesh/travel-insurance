package com.travelinsurance.dto;

import com.travelinsurance.entity.ApplicationStatus;
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
public class PolicyApplicationResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private String userName;
    private Long planId;
    private String planName;
    private Long travelDetailsId;
    private String travellerName;
    private String destination;
    private String departureDate;
    private String returnDate;
    private String applicationNumber;
    private BigDecimal premiumAmount;
    private ApplicationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
