package com.travelinsurance.controller;

import com.travelinsurance.dto.PolicyResponse;
import com.travelinsurance.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    @Autowired
    private PolicyService policyService;

    @GetMapping
    public ResponseEntity<List<PolicyResponse>> getUserPolicies() {
        return ResponseEntity.ok(policyService.getUserPolicies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PolicyResponse> getPolicyById(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.getPolicyById(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadPolicyCertificate(@PathVariable Long id) {
        PolicyResponse policy = policyService.getPolicyById(id);
        byte[] content = policyService.generatePolicyCertificate(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", policy.getDocumentName());

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(content.length)
                .body(content);
    }
}
