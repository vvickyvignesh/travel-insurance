package com.travelinsurance.controller;

import com.travelinsurance.dto.PolicyApplicationResponse;
import com.travelinsurance.service.PolicyApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/applications")
public class AdminApplicationController {

    @Autowired
    private PolicyApplicationService applicationService;

    @GetMapping
    public ResponseEntity<List<PolicyApplicationResponse>> getAllApplications() {
        return ResponseEntity.ok(applicationService.getAllApplications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PolicyApplicationResponse> getApplicationById(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<PolicyApplicationResponse> approveApplication(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.adminApprove(id));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<PolicyApplicationResponse> rejectApplication(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.adminReject(id));
    }
}
