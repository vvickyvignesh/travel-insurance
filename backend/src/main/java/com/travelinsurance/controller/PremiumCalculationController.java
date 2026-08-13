package com.travelinsurance.controller;

import com.travelinsurance.dto.PremiumCalculationResponse;
import com.travelinsurance.service.PremiumCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
public class PremiumCalculationController {

    @Autowired
    private PremiumCalculationService calculationService;

    @PostMapping("/{id}/calculate-premium")
    public ResponseEntity<Map<String, Object>> calculatePremium(@PathVariable Long id) {
        PremiumCalculationResponse response = calculationService.calculatePremium(id);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "Premium calculated successfully");
        body.put("data", response);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}/quote")
    public ResponseEntity<Map<String, Object>> getQuote(@PathVariable Long id) {
        PremiumCalculationResponse response = calculationService.getQuote(id);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("data", response);
        return ResponseEntity.ok(body);
    }
}
