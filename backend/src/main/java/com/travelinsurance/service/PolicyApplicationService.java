package com.travelinsurance.service;

import com.travelinsurance.dto.PolicyApplicationRequest;
import com.travelinsurance.dto.PolicyApplicationResponse;
import com.travelinsurance.entity.*;
import com.travelinsurance.exception.CustomException;
import com.travelinsurance.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PolicyApplicationService {

    @Autowired
    private PolicyApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InsurancePlanRepository planRepository;

    @Autowired
    private TravelDetailsRepository travelDetailsRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private PolicyDocumentRepository documentRepository;

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Authenticated user not found", HttpStatus.UNAUTHORIZED));
    }

    private PolicyApplicationResponse mapToResponse(PolicyApplication app) {
        return PolicyApplicationResponse.builder()
                .id(app.getId())
                .userId(app.getUser().getId())
                .userEmail(app.getUser().getEmail())
                .userName(app.getUser().getName())
                .planId(app.getPlan().getId())
                .planName(app.getPlan().getName())
                .travelDetailsId(app.getTravelDetails().getId())
                .travellerName(app.getTravelDetails().getTravellerName())
                .destination(app.getTravelDetails().getDestination())
                .departureDate(app.getTravelDetails().getDepartureDate().toString())
                .returnDate(app.getTravelDetails().getReturnDate().toString())
                .applicationNumber(app.getApplicationNumber())
                .premiumAmount(app.getPremiumAmount())
                .status(app.getStatus())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .build();
    }

    @Transactional
    public synchronized PolicyApplicationResponse createApplication(PolicyApplicationRequest request) {
        User user = getAuthenticatedUser();

        // 1. Verify selected insurance plan exists and is active
        InsurancePlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new CustomException("Insurance plan not found", HttpStatus.NOT_FOUND));
        if (!plan.getActive()) {
            throw new CustomException("Selected insurance plan is inactive", HttpStatus.BAD_REQUEST);
        }

        // 2. Verify selected travel details exist and belong to the logged-in user
        TravelDetails travelDetails = travelDetailsRepository.findById(request.getTravelDetailsId())
                .orElseThrow(() -> new CustomException("Travel details not found", HttpStatus.NOT_FOUND));
        if (!travelDetails.getUser().getId().equals(user.getId())) {
            throw new CustomException("Travel details do not belong to the authenticated user", HttpStatus.FORBIDDEN);
        }

        // 3. Prevent duplicate active (DRAFT or PENDING_PAYMENT) applications for same details & plan
        boolean duplicateExists = applicationRepository.findByUserId(user.getId()).stream()
                .anyMatch(app -> app.getPlan().getId().equals(request.getPlanId())
                        && app.getTravelDetails().getId().equals(request.getTravelDetailsId())
                        && (app.getStatus() == ApplicationStatus.DRAFT || app.getStatus() == ApplicationStatus.PENDING_PAYMENT));

        if (duplicateExists) {
            throw new CustomException("An active application already exists for this traveler and selected plan", HttpStatus.CONFLICT);
        }

        // 4. Generate unique application number based on max ID
        long nextId = applicationRepository.findMaxId().orElse(0L) + 1;
        String appNumber = String.format("TIP-APP-2026-%06d", nextId);

        PolicyApplication app = PolicyApplication.builder()
                .user(user)
                .plan(plan)
                .travelDetails(travelDetails)
                .applicationNumber(appNumber)
                .premiumAmount(plan.getBasePremium()) // placeholder premium in Phase 4
                .status(ApplicationStatus.DRAFT)
                .build();

        PolicyApplication saved = applicationRepository.save(app);
        return mapToResponse(saved);
    }

    public List<PolicyApplicationResponse> getUserApplications() {
        User user = getAuthenticatedUser();
        return applicationRepository.findByUserId(user.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PolicyApplicationResponse getApplicationById(Long id) {
        User user = getAuthenticatedUser();
        PolicyApplication app = applicationRepository.findById(id)
                .orElseThrow(() -> new CustomException("Policy application not found", HttpStatus.NOT_FOUND));

        // Ownership validation
        if (!app.getUser().getId().equals(user.getId()) && user.getRole() != UserRole.ADMIN) {
            throw new CustomException("Access denied to view this policy application", HttpStatus.FORBIDDEN);
        }

        return mapToResponse(app);
    }

    @Transactional
    public PolicyApplicationResponse updateApplication(Long id, PolicyApplicationRequest request) {
        User user = getAuthenticatedUser();
        PolicyApplication app = applicationRepository.findById(id)
                .orElseThrow(() -> new CustomException("Policy application not found", HttpStatus.NOT_FOUND));

        // Ownership validation
        if (!app.getUser().getId().equals(user.getId())) {
            throw new CustomException("Access denied to edit this policy application", HttpStatus.FORBIDDEN);
        }

        // Business Rule: Can edit only while status is DRAFT
        if (app.getStatus() != ApplicationStatus.DRAFT) {
            throw new CustomException("Application cannot be edited as it is no longer in DRAFT status", HttpStatus.BAD_REQUEST);
        }

        InsurancePlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new CustomException("Insurance plan not found", HttpStatus.NOT_FOUND));
        if (!plan.getActive()) {
            throw new CustomException("Selected insurance plan is inactive", HttpStatus.BAD_REQUEST);
        }

        TravelDetails travelDetails = travelDetailsRepository.findById(request.getTravelDetailsId())
                .orElseThrow(() -> new CustomException("Travel details not found", HttpStatus.NOT_FOUND));
        if (!travelDetails.getUser().getId().equals(user.getId())) {
            throw new CustomException("Travel details do not belong to the authenticated user", HttpStatus.FORBIDDEN);
        }

        app.setPlan(plan);
        app.setTravelDetails(travelDetails);
        app.setPremiumAmount(plan.getBasePremium());

        PolicyApplication updated = applicationRepository.save(app);
        return mapToResponse(updated);
    }

    @Transactional
    public PolicyApplicationResponse submitApplication(Long id) {
        User user = getAuthenticatedUser();
        PolicyApplication app = applicationRepository.findById(id)
                .orElseThrow(() -> new CustomException("Policy application not found", HttpStatus.NOT_FOUND));

        if (!app.getUser().getId().equals(user.getId())) {
            throw new CustomException("Access denied to submit this policy application", HttpStatus.FORBIDDEN);
        }

        // Business Rule: Can submit only when DRAFT
        if (app.getStatus() != ApplicationStatus.DRAFT) {
            throw new CustomException("Application has already been submitted or cancelled", HttpStatus.BAD_REQUEST);
        }

        app.setStatus(ApplicationStatus.PENDING_PAYMENT);
        return mapToResponse(applicationRepository.save(app));
    }

    @Transactional
    public PolicyApplicationResponse cancelApplication(Long id) {
        User user = getAuthenticatedUser();
        PolicyApplication app = applicationRepository.findById(id)
                .orElseThrow(() -> new CustomException("Policy application not found", HttpStatus.NOT_FOUND));

        if (!app.getUser().getId().equals(user.getId())) {
            throw new CustomException("Access denied to cancel this policy application", HttpStatus.FORBIDDEN);
        }

        // Business Rule: Can cancel only DRAFT or PENDING_PAYMENT states
        if (app.getStatus() != ApplicationStatus.DRAFT && app.getStatus() != ApplicationStatus.PENDING_PAYMENT) {
            throw new CustomException("Application cannot be cancelled in its current state", HttpStatus.BAD_REQUEST);
        }

        app.setStatus(ApplicationStatus.CANCELLED);
        return mapToResponse(applicationRepository.save(app));
    }

    // Admin Operations
    public List<PolicyApplicationResponse> getAllApplications() {
        return applicationRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PolicyApplicationResponse adminApprove(Long id) {
        PolicyApplication app = applicationRepository.findById(id)
                .orElseThrow(() -> new CustomException("Policy application not found", HttpStatus.NOT_FOUND));

        if (app.getStatus() != ApplicationStatus.PAYMENT_COMPLETED) {
            throw new CustomException("Application must have completed payment before approval", HttpStatus.BAD_REQUEST);
        }

        app.setStatus(ApplicationStatus.APPROVED);
        PolicyApplication savedApp = applicationRepository.save(app);

        // Generate Policy Number
        String policyNumber = String.format("POL-2026-%06d", savedApp.getId());
        Policy policy = Policy.builder()
                .policyNumber(policyNumber)
                .user(savedApp.getUser())
                .application(savedApp)
                .plan(savedApp.getPlan())
                .coverageAmount(savedApp.getPlan().getCoverageAmount())
                .premiumAmount(savedApp.getPremiumAmount())
                .destination(savedApp.getTravelDetails().getDestination())
                .startDate(savedApp.getTravelDetails().getDepartureDate())
                .endDate(savedApp.getTravelDetails().getReturnDate())
                .status(PolicyStatus.ACTIVE)
                .build();

        Policy savedPolicy = policyRepository.save(policy);

        // Generate Policy Document
        PolicyDocument document = PolicyDocument.builder()
                .policy(savedPolicy)
                .documentName("Policy_Certificate_" + policyNumber + ".txt")
                .documentPath("/api/policies/" + savedPolicy.getId() + "/download")
                .documentType("TXT")
                .build();

        documentRepository.save(document);

        return mapToResponse(savedApp);
    }

    @Transactional
    public PolicyApplicationResponse adminReject(Long id) {
        PolicyApplication app = applicationRepository.findById(id)
                .orElseThrow(() -> new CustomException("Policy application not found", HttpStatus.NOT_FOUND));

        app.setStatus(ApplicationStatus.REJECTED);
        return mapToResponse(applicationRepository.save(app));
    }
}
