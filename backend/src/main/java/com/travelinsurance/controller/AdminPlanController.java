package com.travelinsurance.controller;

import com.travelinsurance.dto.InsurancePlanRequest;
import com.travelinsurance.dto.InsurancePlanResponse;
import com.travelinsurance.service.InsurancePlanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/plans")
public class AdminPlanController {

    @Autowired
    private InsurancePlanService planService;

    @GetMapping
    public ResponseEntity<List<InsurancePlanResponse>> getAllPlans() {
        return ResponseEntity.ok(planService.getAllPlans());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InsurancePlanResponse> getPlanById(@PathVariable Long id) {
        return ResponseEntity.ok(planService.getPlanById(id, false));
    }

    @PostMapping
    public ResponseEntity<InsurancePlanResponse> createPlan(@Valid @RequestBody InsurancePlanRequest request) {
        InsurancePlanResponse created = planService.createPlan(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InsurancePlanResponse> updatePlan(@PathVariable Long id, @Valid @RequestBody InsurancePlanRequest request) {
        InsurancePlanResponse updated = planService.updatePlan(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePlan(@PathVariable Long id) {
        planService.deletePlan(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Plan deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<InsurancePlanResponse> activatePlan(@PathVariable Long id) {
        InsurancePlanResponse activated = planService.activatePlan(id);
        return ResponseEntity.ok(activated);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<InsurancePlanResponse> deactivatePlan(@PathVariable Long id) {
        InsurancePlanResponse deactivated = planService.deactivatePlan(id);
        return ResponseEntity.ok(deactivated);
    }
}
