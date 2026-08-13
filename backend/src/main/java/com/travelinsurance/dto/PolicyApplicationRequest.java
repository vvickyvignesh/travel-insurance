package com.travelinsurance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyApplicationRequest {

    @NotNull(message = "Plan ID is required")
    private Long planId;

    @NotNull(message = "Travel details ID is required")
    private Long travelDetailsId;
}
