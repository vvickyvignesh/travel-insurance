package com.travelinsurance.controller;

import com.travelinsurance.dto.PaymentResponse;
import com.travelinsurance.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/payments")
public class AdminPaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPayments() {
        List<PaymentResponse> payments = paymentService.getAllPaymentsForAdmin();
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("data", payments);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPaymentById(@PathVariable Long id) {
        PaymentResponse response = paymentService.getPaymentById(id);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("data", response);
        return ResponseEntity.ok(body);
    }
}
