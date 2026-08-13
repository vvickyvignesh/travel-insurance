package com.travelinsurance.service;

import com.travelinsurance.dto.InsurancePlanRequest;
import com.travelinsurance.dto.InsurancePlanResponse;
import com.travelinsurance.entity.InsurancePlan;
import com.travelinsurance.exception.CustomException;
import com.travelinsurance.repository.InsurancePlanRepository;
import com.travelinsurance.repository.PolicyApplicationRepository;
import com.travelinsurance.repository.PolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InsurancePlanService {

    @Autowired
    private InsurancePlanRepository planRepository;

    @Autowired
    private PolicyApplicationRepository applicationRepository;

    @Autowired
    private PolicyRepository policyRepository;

    private InsurancePlanResponse mapToResponse(InsurancePlan plan) {
        return InsurancePlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .coverageAmount(plan.getCoverageAmount())
                .medicalCoverage(plan.getMedicalCoverage())
                .baggageCoverage(plan.getBaggageCoverage())
                .tripCancellation(plan.getTripCancellation())
                .emergencyAssistance(plan.getEmergencyAssistance())
                .basePremium(plan.getBasePremium())
                .active(plan.getActive())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }

    @Transactional
    public InsurancePlanResponse createPlan(InsurancePlanRequest request) {
        if (planRepository.existsByName(request.getName().trim())) {
            throw new CustomException("Insurance plan name already exists", HttpStatus.CONFLICT);
        }

        InsurancePlan plan = InsurancePlan.builder()
                .name(request.getName().trim())
                .description(request.getDescription().trim())
                .coverageAmount(request.getCoverageAmount())
                .medicalCoverage(request.getMedicalCoverage())
                .baggageCoverage(request.getBaggageCoverage())
                .tripCancellation(request.getTripCancellation())
                .emergencyAssistance(request.getEmergencyAssistance() != null ? request.getEmergencyAssistance() : true)
                .basePremium(request.getBasePremium())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        InsurancePlan saved = planRepository.save(plan);
        return mapToResponse(saved);
    }

    public List<InsurancePlanResponse> getActivePlans() {
        return planRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<InsurancePlanResponse> getAllPlans() {
        return planRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public InsurancePlanResponse getPlanById(Long id, boolean activeOnly) {
        InsurancePlan plan;
        if (activeOnly) {
            plan = planRepository.findByIdAndActiveTrue(id)
                    .orElseThrow(() -> new CustomException("Active plan not found", HttpStatus.NOT_FOUND));
        } else {
            plan = planRepository.findById(id)
                    .orElseThrow(() -> new CustomException("Plan not found", HttpStatus.NOT_FOUND));
        }
        return mapToResponse(plan);
    }

    @Transactional
    public InsurancePlanResponse updatePlan(Long id, InsurancePlanRequest request) {
        InsurancePlan plan = planRepository.findById(id)
                .orElseThrow(() -> new CustomException("Plan not found", HttpStatus.NOT_FOUND));

        if (planRepository.existsByNameAndIdNot(request.getName().trim(), id)) {
            throw new CustomException("Insurance plan name already exists", HttpStatus.CONFLICT);
        }

        plan.setName(request.getName().trim());
        plan.setDescription(request.getDescription().trim());
        plan.setCoverageAmount(request.getCoverageAmount());
        plan.setMedicalCoverage(request.getMedicalCoverage());
        plan.setBaggageCoverage(request.getBaggageCoverage());
        plan.setTripCancellation(request.getTripCancellation());
        plan.setEmergencyAssistance(request.getEmergencyAssistance() != null ? request.getEmergencyAssistance() : plan.getEmergencyAssistance());
        plan.setBasePremium(request.getBasePremium());
        if (request.getActive() != null) {
            plan.setActive(request.getActive());
        }

        InsurancePlan updated = planRepository.save(plan);
        return mapToResponse(updated);
    }

    @Transactional
    public void deletePlan(Long id) {
        InsurancePlan plan = planRepository.findById(id)
                .orElseThrow(() -> new CustomException("Plan not found", HttpStatus.NOT_FOUND));

        // Safe Delete check: check reference in application or policies
        if (applicationRepository.existsByPlanId(id) || policyRepository.existsByPlanId(id)) {
            throw new CustomException("This plan is already being used. Deactivate it instead of deleting it.", HttpStatus.BAD_REQUEST);
        }

        planRepository.delete(plan);
    }

    @Transactional
    public InsurancePlanResponse activatePlan(Long id) {
        InsurancePlan plan = planRepository.findById(id)
                .orElseThrow(() -> new CustomException("Plan not found", HttpStatus.NOT_FOUND));
        plan.setActive(true);
        return mapToResponse(planRepository.save(plan));
    }

    @Transactional
    public InsurancePlanResponse deactivatePlan(Long id) {
        InsurancePlan plan = planRepository.findById(id)
                .orElseThrow(() -> new CustomException("Plan not found", HttpStatus.NOT_FOUND));
        plan.setActive(false);
        return mapToResponse(planRepository.save(plan));
    }

    public List<InsurancePlanResponse> searchPlans(String keyword, boolean activeOnly) {
        List<InsurancePlan> plans;
        if (activeOnly) {
            plans = planRepository.findByActiveTrueAndNameContainingIgnoreCase(keyword);
        } else {
            plans = planRepository.findByNameContainingIgnoreCase(keyword);
        }
        return plans.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}
