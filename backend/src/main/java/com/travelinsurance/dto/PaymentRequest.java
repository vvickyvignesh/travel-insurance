package com.travelinsurance.dto;

import com.travelinsurance.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {

    @NotNull(message = "Application ID is required")
    private Long applicationId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}
