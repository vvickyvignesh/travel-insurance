package com.travelinsurance.controller;

import com.travelinsurance.dto.PaymentRequest;
import com.travelinsurance.dto.PaymentResponse;
import com.travelinsurance.dto.PaymentReceiptResponse;
import com.travelinsurance.entity.PaymentStatus;
import com.travelinsurance.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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
        Map<String, Object> data = new HashMap<>();

        if (response.getStatus() == PaymentStatus.SUCCESS) {
            body.put("success", true);
            body.put("message", "Payment completed successfully");
            
            data.put("id", response.getId());
            data.put("transactionId", response.getTransactionId());
            data.put("applicationNumber", response.getApplicationNumber());
            data.put("amount", response.getAmount());
            data.put("paymentMethod", response.getPaymentMethod());
            data.put("status", response.getStatus());
            body.put("data", data);
            
            return new ResponseEntity<>(body, HttpStatus.CREATED);
        } else {
            body.put("success", false);
            body.put("message", "Payment failed. Please try again.");
            
            data.put("transactionId", response.getTransactionId());
            data.put("status", response.getStatus());
            body.put("data", data);
            
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPaymentById(@PathVariable Long id) {
        PaymentResponse response = paymentService.getPaymentById(id);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("data", response);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}/receipt")
    public ResponseEntity<PaymentReceiptResponse> getPaymentReceipt(@PathVariable Long id) {
        PaymentReceiptResponse receipt = paymentService.generateReceipt(id);
        return ResponseEntity.ok(receipt);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserPayments() {
        List<PaymentResponse> payments = paymentService.getUserPayments();
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("data", payments);
        return ResponseEntity.ok(body);
    }
}
