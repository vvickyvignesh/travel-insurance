package com.travelinsurance.service;

import com.travelinsurance.dto.TravelDetailsRequest;
import com.travelinsurance.dto.TravelDetailsResponse;
import com.travelinsurance.entity.TravelDetails;
import com.travelinsurance.entity.User;
import com.travelinsurance.entity.UserRole;
import com.travelinsurance.exception.CustomException;
import com.travelinsurance.repository.PolicyApplicationRepository;
import com.travelinsurance.repository.TravelDetailsRepository;
import com.travelinsurance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TravelDetailsService {

    @Autowired
    private TravelDetailsRepository travelDetailsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PolicyApplicationRepository applicationRepository;

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Authenticated user not found", HttpStatus.UNAUTHORIZED));
    }

    private void validateDates(TravelDetailsRequest request) {
        if (request.getDateOfBirth().isAfter(request.getDepartureDate()) || request.getDateOfBirth().isEqual(request.getDepartureDate())) {
            throw new CustomException("Date of birth must be before the departure date", HttpStatus.BAD_REQUEST);
        }
        if (request.getDepartureDate().isAfter(request.getReturnDate())) {
            throw new CustomException("Departure date cannot be after the return date", HttpStatus.BAD_REQUEST);
        }
    }

    private TravelDetailsResponse mapToResponse(TravelDetails details) {
        return TravelDetailsResponse.builder()
                .id(details.getId())
                .userId(details.getUser().getId())
                .travellerName(details.getTravellerName())
                .dateOfBirth(details.getDateOfBirth())
                .passportNumber(details.getPassportNumber())
                .phone(details.getPhone())
                .destination(details.getDestination())
                .departureDate(details.getDepartureDate())
                .returnDate(details.getReturnDate())
                .tripType(details.getTripType())
                .travelPurpose(details.getTravelPurpose())
                .createdAt(details.getCreatedAt())
                .updatedAt(details.getUpdatedAt())
                .build();
    }

    @Transactional
    public TravelDetailsResponse createTravelDetails(TravelDetailsRequest request) {
        validateDates(request);
        User user = getAuthenticatedUser();

        TravelDetails details = TravelDetails.builder()
                .user(user)
                .travellerName(request.getTravellerName().trim())
                .dateOfBirth(request.getDateOfBirth())
                .passportNumber(request.getPassportNumber().trim())
                .phone(request.getPhone().trim())
                .destination(request.getDestination().trim())
                .departureDate(request.getDepartureDate())
                .returnDate(request.getReturnDate())
                .tripType(request.getTripType())
                .travelPurpose(request.getTravelPurpose())
                .build();

        TravelDetails saved = travelDetailsRepository.save(details);
        return mapToResponse(saved);
    }

    public List<TravelDetailsResponse> getUserTravelDetails() {
        User user = getAuthenticatedUser();
        return travelDetailsRepository.findByUserId(user.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TravelDetailsResponse getTravelDetailsById(Long id) {
        User user = getAuthenticatedUser();
        TravelDetails details = travelDetailsRepository.findById(id)
                .orElseThrow(() -> new CustomException("Travel details record not found", HttpStatus.NOT_FOUND));

        // Ownership validation
        if (!details.getUser().getId().equals(user.getId()) && user.getRole() != UserRole.ADMIN) {
            throw new CustomException("Access denied to view this travel details record", HttpStatus.FORBIDDEN);
        }

        return mapToResponse(details);
    }

    @Transactional
    public TravelDetailsResponse updateTravelDetails(Long id, TravelDetailsRequest request) {
        validateDates(request);
        User user = getAuthenticatedUser();
        TravelDetails details = travelDetailsRepository.findById(id)
                .orElseThrow(() -> new CustomException("Travel details record not found", HttpStatus.NOT_FOUND));

        // Ownership validation
        if (!details.getUser().getId().equals(user.getId())) {
            throw new CustomException("Access denied to edit this travel details record", HttpStatus.FORBIDDEN);
        }

        details.setTravellerName(request.getTravellerName().trim());
        details.setDateOfBirth(request.getDateOfBirth());
        details.setPassportNumber(request.getPassportNumber().trim());
        details.setPhone(request.getPhone().trim());
        details.setDestination(request.getDestination().trim());
        details.setDepartureDate(request.getDepartureDate());
        details.setReturnDate(request.getReturnDate());
        details.setTripType(request.getTripType());
        details.setTravelPurpose(request.getTravelPurpose());

        TravelDetails updated = travelDetailsRepository.save(details);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteTravelDetails(Long id) {
        User user = getAuthenticatedUser();
        TravelDetails details = travelDetailsRepository.findById(id)
                .orElseThrow(() -> new CustomException("Travel details record not found", HttpStatus.NOT_FOUND));

        // Ownership validation
        if (!details.getUser().getId().equals(user.getId())) {
            throw new CustomException("Access denied to delete this travel details record", HttpStatus.FORBIDDEN);
        }

        // Check if travel details are referenced in ANY applications
        // (Wait! Even if it is in a draft or submitted application, let's block or check as requested: "Allow deletion only when the travel details are not being used by a submitted application.")
        // If we want to be strict and protect DB integrity, we should block if they exist in ANY policy applications because of the Foreign Key constraint: `policy_applications.travel_details_id`.
        // If a DB foreign key ON DELETE CASCADE is configured, it would delete the application too. But schema.sql has: `travel_details_id BIGINT NOT NULL REFERENCES travel_details(id) ON DELETE CASCADE`.
        // Even though CASCADE is there, we should explicitly check: if there is a submitted application (e.g. status PENDING_PAYMENT, APPROVED, etc.), block it!
        // Let's implement checking.
        // Get all applications using these travel details. If any are PENDING_PAYMENT or PAYMENT_COMPLETED or APPROVED or REJECTED or CANCELLED (any non-DRAFT, basically), block it.
        // Wait, the schema has cascading deletes. But to follow business rules safely:
        // "Allow deletion only when the travel details are not being used by a submitted application."
        // We will query applications using travelDetailsId.
        boolean usedInSubmitted = applicationRepository.findAll().stream()
                .anyMatch(app -> app.getTravelDetails().getId().equals(id) && app.getStatus() != com.travelinsurance.entity.ApplicationStatus.DRAFT);
        if (usedInSubmitted) {
            throw new CustomException("Cannot delete travel details associated with a submitted application.", HttpStatus.BAD_REQUEST);
        }

        travelDetailsRepository.delete(details);
    }
}
