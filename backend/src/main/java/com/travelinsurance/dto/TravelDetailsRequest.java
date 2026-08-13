package com.travelinsurance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelDetailsRequest {

    @NotBlank(message = "Traveller name is required")
    @Size(max = 100, message = "Traveller name must be less than 100 characters")
    private String travellerName;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Passport number is required")
    @Size(max = 50, message = "Passport number must be less than 50 characters")
    private String passportNumber;

    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone must be less than 20 characters")
    private String phone;

    @NotBlank(message = "Destination is required")
    @Size(max = 100, message = "Destination must be less than 100 characters")
    private String destination;

    @NotNull(message = "Departure date is required")
    private LocalDate departureDate;

    @NotNull(message = "Return date is required")
    private LocalDate returnDate;

    @NotBlank(message = "Trip type is required")
    @Size(max = 50, message = "Trip type must be less than 50 characters")
    private String tripType;

    @NotBlank(message = "Travel purpose is required")
    @Size(max = 100, message = "Travel purpose must be less than 100 characters")
    private String travelPurpose;
}
