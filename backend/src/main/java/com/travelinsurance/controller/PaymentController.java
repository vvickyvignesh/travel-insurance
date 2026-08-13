package com.travelinsurance.controller;

import com.travelinsurance.dto.PaymentRequest;
import com.travelinsurance.dto.PaymentResponse;
import com.travelinsurance.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> processPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "Payment processed successfully");
        body.put("data", response);

        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }
}
