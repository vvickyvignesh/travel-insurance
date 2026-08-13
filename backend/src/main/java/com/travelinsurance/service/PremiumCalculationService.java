package com.travelinsurance.service;

import com.travelinsurance.dto.PremiumCalculationResponse;
import com.travelinsurance.entity.*;
import com.travelinsurance.exception.CustomException;
import com.travelinsurance.repository.PolicyApplicationRepository;
import com.travelinsurance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Service
public class PremiumCalculationService {

    @Autowired
    private PolicyApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Authenticated user not found", HttpStatus.UNAUTHORIZED));
    }

    public long calculateTripDuration(LocalDate departureDate, LocalDate returnDate) {
        if (departureDate.isAfter(returnDate)) {
            throw new CustomException("Departure date cannot be after return date", HttpStatus.BAD_REQUEST);
        }
        return ChronoUnit.DAYS.between(departureDate, returnDate) + 1;
    }

    public int calculateTravellerAge(LocalDate dateOfBirth) {
        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw new CustomException("Date of birth cannot be in the future", HttpStatus.BAD_REQUEST);
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    public BigDecimal getDurationMultiplier(long durationDays) {
        if (durationDays >= 1 && durationDays <= 7) return new BigDecimal("1.00");
        if (durationDays >= 8 && durationDays <= 15) return new BigDecimal("1.20");
        if (durationDays >= 16 && durationDays <= 30) return new BigDecimal("1.50");
        if (durationDays >= 31 && durationDays <= 60) return new BigDecimal("2.00");
        if (durationDays >= 61) return new BigDecimal("2.50");
        return new BigDecimal("1.00"); // default fallback
    }

    public BigDecimal getAgeMultiplier(int age) {
        if (age >= 0 && age <= 17) return new BigDecimal("1.00");
        if (age >= 18 && age <= 40) return new BigDecimal("1.00");
        if (age >= 41 && age <= 60) return new BigDecimal("1.20");
        if (age >= 61 && age <= 70) return new BigDecimal("1.50");
        if (age >= 71) return new BigDecimal("2.00");
        return new BigDecimal("1.00"); // default fallback
    }

    public BigDecimal getDestinationMultiplier(String destination) {
        if (destination == null || destination.trim().isEmpty()) {
            return new BigDecimal("1.40"); // other international
        }

        String destLower = destination.trim().toLowerCase();

        // Domestic List
        List<String> domesticKeywords = Arrays.asList("india", "tamil nadu", "kerala", "karnataka", "delhi", "mumbai", "domestic");
        if (domesticKeywords.stream().anyMatch(destLower::contains)) {
            return new BigDecimal("1.00");
        }

        // Asia List
        List<String> asiaKeywords = Arrays.asList("singapore", "malaysia", "thailand", "japan", "south korea", "asia");
        if (asiaKeywords.stream().anyMatch(destLower::contains)) {
            return new BigDecimal("1.20");
        }

        // Europe List
        List<String> europeKeywords = Arrays.asList("france", "germany", "italy", "spain", "switzerland", "europe", "uk", "united kingdom");
        if (europeKeywords.stream().anyMatch(destLower::contains)) {
            return new BigDecimal("1.50");
        }

        // USA / Canada List
        List<String> usaCanadaKeywords = Arrays.asList("usa", "united states", "canada", "america");
        if (usaCanadaKeywords.stream().anyMatch(destLower::contains)) {
            return new BigDecimal("1.80");
        }

        return new BigDecimal("1.40"); // Default: other international
    }

    public BigDecimal getTripTypeMultiplier(String tripType) {
        if (tripType == null) return new BigDecimal("1.00");
        try {
            switch (tripType.trim().toUpperCase()) {
                case "ONE_WAY":
                    return new BigDecimal("1.00");
                case "ROUND_TRIP":
                    return new BigDecimal("1.10");
                case "MULTI_CITY":
                    return new BigDecimal("1.25");
                default:
                    return new BigDecimal("1.00");
            }
        } catch (IllegalArgumentException e) {
            return new BigDecimal("1.00");
        }
    }

    @Transactional
    public PremiumCalculationResponse calculatePremium(Long id) {
        User user = getAuthenticatedUser();
        PolicyApplication app = applicationRepository.findById(id)
                .orElseThrow(() -> new CustomException("Policy application not found", HttpStatus.NOT_FOUND));

        // Ownership validation
        if (!app.getUser().getId().equals(user.getId()) && user.getRole() != UserRole.ADMIN) {
            throw new CustomException("Access denied to calculate premium for this application", HttpStatus.FORBIDDEN);
        }

        // Status rule validation
        if (app.getStatus() != ApplicationStatus.DRAFT && app.getStatus() != ApplicationStatus.PENDING_PAYMENT) {
            throw new CustomException("Premium can only be calculated for DRAFT or PENDING_PAYMENT applications", HttpStatus.BAD_REQUEST);
        }

        TravelDetails td = app.getTravelDetails();
        InsurancePlan plan = app.getPlan();

        // 1. Calculations
        long duration = calculateTripDuration(td.getDepartureDate(), td.getReturnDate());
        int age = calculateTravellerAge(td.getDateOfBirth());

        BigDecimal durationMult = getDurationMultiplier(duration);
        BigDecimal ageMult = getAgeMultiplier(age);
        BigDecimal destMult = getDestinationMultiplier(td.getDestination());
        BigDecimal typeMult = getTripTypeMultiplier(td.getTripType());

        BigDecimal base = plan.getBasePremium();
        BigDecimal finalPremium = base
                .multiply(durationMult)
                .multiply(ageMult)
                .multiply(destMult)
                .multiply(typeMult)
                .setScale(2, RoundingMode.HALF_UP);

        // 2. Persist premium back to the database
        app.setPremiumAmount(finalPremium);
        applicationRepository.save(app);

        return PremiumCalculationResponse.builder()
                .applicationId(app.getId())
                .applicationNumber(app.getApplicationNumber())
                .planName(plan.getName())
                .basePremium(base)
                .tripDuration(duration)
                .durationMultiplier(durationMult)
                .travellerAge(age)
                .ageMultiplier(ageMult)
                .destination(td.getDestination())
                .destinationMultiplier(destMult)
                .tripType(td.getTripType())
                .tripTypeMultiplier(typeMult)
                .finalPremium(finalPremium)
                .currency("INR")
                .calculatedAt(LocalDateTime.now())
                .build();
    }

    public PremiumCalculationResponse getQuote(Long id) {
        User user = getAuthenticatedUser();
        PolicyApplication app = applicationRepository.findById(id)
                .orElseThrow(() -> new CustomException("Policy application not found", HttpStatus.NOT_FOUND));

        // Ownership validation
        if (!app.getUser().getId().equals(user.getId()) && user.getRole() != UserRole.ADMIN) {
            throw new CustomException("Access denied to view quote details", HttpStatus.FORBIDDEN);
        }

        if (app.getPremiumAmount() == null) {
            throw new CustomException("Premium has not been calculated for this application", HttpStatus.BAD_REQUEST);
        }

        // Return a mock recalculation layout for details representation
        TravelDetails td = app.getTravelDetails();
        InsurancePlan plan = app.getPlan();

        long duration = calculateTripDuration(td.getDepartureDate(), td.getReturnDate());
        int age = calculateTravellerAge(td.getDateOfBirth());

        return PremiumCalculationResponse.builder()
                .applicationId(app.getId())
                .applicationNumber(app.getApplicationNumber())
                .planName(plan.getName())
                .basePremium(plan.getBasePremium())
                .tripDuration(duration)
                .durationMultiplier(getDurationMultiplier(duration))
                .travellerAge(age)
                .ageMultiplier(getAgeMultiplier(age))
                .destination(td.getDestination())
                .destinationMultiplier(getDestinationMultiplier(td.getDestination()))
                .tripType(td.getTripType())
                .tripTypeMultiplier(getTripTypeMultiplier(td.getTripType()))
                .finalPremium(app.getPremiumAmount())
                .currency("INR")
                .calculatedAt(app.getUpdatedAt() != null ? app.getUpdatedAt() : app.getCreatedAt())
                .build();
    }
}
