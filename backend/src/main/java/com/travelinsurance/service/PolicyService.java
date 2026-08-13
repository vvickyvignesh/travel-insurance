package com.travelinsurance.service;

import com.travelinsurance.dto.PolicyResponse;
import com.travelinsurance.entity.Policy;
import com.travelinsurance.entity.PolicyDocument;
import com.travelinsurance.entity.User;
import com.travelinsurance.entity.UserRole;
import com.travelinsurance.exception.CustomException;
import com.travelinsurance.repository.PolicyDocumentRepository;
import com.travelinsurance.repository.PolicyRepository;
import com.travelinsurance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PolicyService {

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private PolicyDocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Authenticated user not found", HttpStatus.UNAUTHORIZED));
    }

    private PolicyResponse mapToResponse(Policy policy) {
        PolicyDocument doc = documentRepository.findByPolicyId(policy.getId()).orElse(null);
        return PolicyResponse.builder()
                .id(policy.getId())
                .policyNumber(policy.getPolicyNumber())
                .applicationId(policy.getApplication().getId())
                .applicationNumber(policy.getApplication().getApplicationNumber())
                .planId(policy.getPlan().getId())
                .planName(policy.getPlan().getName())
                .userName(policy.getUser().getName())
                .userEmail(policy.getUser().getEmail())
                .coverageAmount(policy.getCoverageAmount())
                .premiumAmount(policy.getPremiumAmount())
                .destination(policy.getDestination())
                .startDate(policy.getStartDate())
                .endDate(policy.getEndDate())
                .status(policy.getStatus())
                .createdAt(policy.getCreatedAt())
                .documentName(doc != null ? doc.getDocumentName() : null)
                .documentPath(doc != null ? doc.getDocumentPath() : null)
                .build();
    }

    public List<PolicyResponse> getUserPolicies() {
        User user = getAuthenticatedUser();
        List<Policy> policies;
        if (user.getRole() == UserRole.ADMIN) {
            policies = policyRepository.findAll();
        } else {
            policies = policyRepository.findAll().stream()
                    .filter(p -> p.getUser().getId().equals(user.getId()))
                    .collect(Collectors.toList());
        }
        return policies.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public PolicyResponse getPolicyById(Long id) {
        User user = getAuthenticatedUser();
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new CustomException("Policy not found", HttpStatus.NOT_FOUND));

        if (!policy.getUser().getId().equals(user.getId()) && user.getRole() != UserRole.ADMIN) {
            throw new CustomException("Access denied to view this policy", HttpStatus.FORBIDDEN);
        }

        return mapToResponse(policy);
    }

    public byte[] generatePolicyCertificate(Long policyId) {
        User user = getAuthenticatedUser();
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new CustomException("Policy not found", HttpStatus.NOT_FOUND));

        if (!policy.getUser().getId().equals(user.getId()) && user.getRole() != UserRole.ADMIN) {
            throw new CustomException("Access denied to download this policy certificate", HttpStatus.FORBIDDEN);
        }

        // Generate a beautiful text-based policy certificate
        StringBuilder cert = new StringBuilder();
        cert.append("========================================================================\n");
        cert.append("                         TRAVEL SHIELD INSURANCE                       \n");
        cert.append("                       OFFICIAL POLICY CERTIFICATE                     \n");
        cert.append("========================================================================\n\n");
        cert.append(String.format("Policy Number       : %s\n", policy.getPolicyNumber()));
        cert.append(String.format("Application No      : %s\n", policy.getApplication().getApplicationNumber()));
        cert.append(String.format("Status              : %s\n", policy.getStatus().name()));
        cert.append(String.format("Issue Date          : %s\n\n", policy.getCreatedAt()));
        
        cert.append("------------------------- INSURED DETAILS ------------------------------\n");
        cert.append(String.format("Insured Name        : %s\n", policy.getApplication().getTravelDetails().getTravellerName()));
        cert.append(String.format("Passport Number     : %s\n", policy.getApplication().getTravelDetails().getPassportNumber()));
        cert.append(String.format("Date of Birth       : %s\n", policy.getApplication().getTravelDetails().getDateOfBirth()));
        cert.append(String.format("Contact Email       : %s\n\n", policy.getUser().getEmail()));

        cert.append("------------------------- COVERAGE DETAILS -----------------------------\n");
        cert.append(String.format("Insurance Plan      : %s\n", policy.getPlan().getName()));
        cert.append(String.format("Destination Country : %s\n", policy.getDestination()));
        cert.append(String.format("Travel Start Date   : %s\n", policy.getStartDate()));
        cert.append(String.format("Travel End Date     : %s\n", policy.getEndDate()));
        cert.append(String.format("Total Coverage      : INR %,.2f\n", policy.getCoverageAmount()));
        cert.append(String.format("Total Premium Paid  : INR %,.2f\n\n", policy.getPremiumAmount()));

        cert.append("----------------------- SPECIFIC COVERAGES -----------------------------\n");
        cert.append(String.format("Medical Expenses Limit : INR %,.2f\n", policy.getPlan().getMedicalCoverage()));
        cert.append(String.format("Baggage Loss Limit     : INR %,.2f\n", policy.getPlan().getBaggageCoverage()));
        cert.append(String.format("Trip Cancellation Limit: INR %,.2f\n", policy.getPlan().getTripCancellation()));
        cert.append(String.format("Emergency Assistance   : %s\n\n", policy.getPlan().getEmergencyAssistance() ? "YES" : "NO"));

        cert.append("========================================================================\n");
        cert.append("Thank you for choosing Travel Shield. Wish you a safe and pleasant journey!\n");
        cert.append("For support, contact claims@travelshield.com or call +1-800-TRAVEL-SAFE\n");
        cert.append("========================================================================\n");

        return cert.toString().getBytes(StandardCharsets.UTF_8);
    }
}
