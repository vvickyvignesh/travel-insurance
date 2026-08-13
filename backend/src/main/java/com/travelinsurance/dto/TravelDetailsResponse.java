package com.travelinsurance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelDetailsResponse {
    private Long id;
    private Long userId;
    private String travellerName;
    private LocalDate dateOfBirth;
    private String passportNumber;
    private String phone;
    private String destination;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private String tripType;
    private String travelPurpose;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
