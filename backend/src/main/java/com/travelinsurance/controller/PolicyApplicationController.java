package com.travelinsurance.controller;

import com.travelinsurance.dto.PolicyApplicationRequest;
import com.travelinsurance.dto.PolicyApplicationResponse;
import com.travelinsurance.service.PolicyApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
public class PolicyApplicationController {

    @Autowired
    private PolicyApplicationService applicationService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createApplication(
            @Valid @RequestBody PolicyApplicationRequest request) {
        PolicyApplicationResponse response = applicationService.createApplication(request);
        
        Map<String, Object> innerData = new HashMap<>();
        innerData.put("id", response.getId());
        innerData.put("applicationNumber", response.getApplicationNumber());
        innerData.put("status", response.getStatus().name());
        innerData.put("planId", response.getPlanId());
        innerData.put("travelDetailsId", response.getTravelDetailsId());

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "Application created successfully");
        body.put("data", innerData);

        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PolicyApplicationResponse>> getUserApplications() {
        return ResponseEntity.ok(applicationService.getUserApplications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PolicyApplicationResponse> getApplicationById(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PolicyApplicationResponse> updateApplication(
            @PathVariable Long id, @Valid @RequestBody PolicyApplicationRequest request) {
        return ResponseEntity.ok(applicationService.updateApplication(id, request));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<Map<String, Object>> submitApplication(@PathVariable Long id) {
        PolicyApplicationResponse response = applicationService.submitApplication(id);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "Application submitted successfully");
        body.put("data", response);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelApplication(@PathVariable Long id) {
        PolicyApplicationResponse response = applicationService.cancelApplication(id);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "Application cancelled successfully");
        body.put("data", response);
        return ResponseEntity.ok(body);
    }

    @Autowired
    private com.travelinsurance.service.PaymentService paymentService;

    @GetMapping("/{id}/payment")
    public ResponseEntity<Map<String, Object>> getApplicationPayment(@PathVariable Long id) {
        com.travelinsurance.dto.PaymentResponse response = paymentService.getPaymentByApplication(id);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("data", response);
        return ResponseEntity.ok(body);
    }
}
