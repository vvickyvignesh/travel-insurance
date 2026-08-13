package com.travelinsurance.controller;

import com.travelinsurance.dto.InsurancePlanResponse;
import com.travelinsurance.service.InsurancePlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
public class InsurancePlanController {

    @Autowired
    private InsurancePlanService planService;

    @GetMapping
    public ResponseEntity<List<InsurancePlanResponse>> getActivePlans() {
        return ResponseEntity.ok(planService.getActivePlans());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InsurancePlanResponse> getActivePlanById(@PathVariable Long id) {
        return ResponseEntity.ok(planService.getPlanById(id, true));
    }

    @GetMapping("/search")
    public ResponseEntity<List<InsurancePlanResponse>> searchActivePlans(@RequestParam String keyword) {
        return ResponseEntity.ok(planService.searchPlans(keyword, true));
    }
}
