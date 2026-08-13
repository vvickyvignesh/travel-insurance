package com.travelinsurance.service;

import com.travelinsurance.dto.PaymentRequest;
import com.travelinsurance.dto.PaymentResponse;
import com.travelinsurance.entity.*;
import com.travelinsurance.exception.CustomException;
import com.travelinsurance.repository.PaymentRepository;
import com.travelinsurance.repository.PolicyApplicationRepository;
import com.travelinsurance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PolicyApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Authenticated user not found", HttpStatus.UNAUTHORIZED));
    }

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        User user = getAuthenticatedUser();

        PolicyApplication application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new CustomException("Policy application not found", HttpStatus.NOT_FOUND));

        // Validate Ownership
        if (!application.getUser().getId().equals(user.getId())) {
            throw new CustomException("Access denied to pay for this application", HttpStatus.FORBIDDEN);
        }

        // Verify status is PENDING_PAYMENT
        if (application.getStatus() != ApplicationStatus.PENDING_PAYMENT) {
            throw new CustomException("Application is not in PENDING_PAYMENT status", HttpStatus.BAD_REQUEST);
        }

        // Check if successful payment already exists
        boolean paymentExists = paymentRepository.findByApplicationId(application.getId())
                .map(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .orElse(false);

        if (paymentExists) {
            throw new CustomException("Payment has already been successfully processed for this application", HttpStatus.BAD_REQUEST);
        }

        // Generate Transaction ID
        String transactionId = "TXN-" + UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 12);

        // Build Payment Entity
        Payment payment = Payment.builder()
                .application(application)
                .transactionId(transactionId)
                .amount(application.getPremiumAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.SUCCESS)
                .paymentDate(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // Update Application status to PAYMENT_COMPLETED
        application.setStatus(ApplicationStatus.PAYMENT_COMPLETED);
        applicationRepository.save(application);

        return PaymentResponse.builder()
                .id(savedPayment.getId())
                .applicationId(application.getId())
                .applicationNumber(application.getApplicationNumber())
                .transactionId(savedPayment.getTransactionId())
                .amount(savedPayment.getAmount())
                .paymentMethod(savedPayment.getPaymentMethod())
                .status(savedPayment.getStatus())
                .paymentDate(savedPayment.getPaymentDate())
                .build();
    }
}
