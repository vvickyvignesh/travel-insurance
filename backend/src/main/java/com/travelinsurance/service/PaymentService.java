package com.travelinsurance.service;

import com.travelinsurance.dto.PaymentRequest;
import com.travelinsurance.dto.PaymentResponse;
import com.travelinsurance.dto.PaymentReceiptResponse;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

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

    public String generateTransactionId() {
        long nextId = paymentRepository.count() + 1;
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("TIP-TXN-%s-%06d", dateStr, nextId);
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

        // Verify premium exists and is > 0
        BigDecimal premium = application.getPremiumAmount();
        if (premium == null || premium.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("Premium has not been calculated or is invalid", HttpStatus.BAD_REQUEST);
        }

        // Check whether successful payment already exists (Duplicate Payment Protection)
        boolean paymentExists = paymentRepository.existsByApplicationIdAndStatus(application.getId(), PaymentStatus.SUCCESS);
        if (paymentExists) {
            throw new CustomException("Payment has already been completed for this application.", HttpStatus.CONFLICT);
        }

        // Generate Transaction ID in backend
        String transactionId = generateTransactionId();

        // Process demo result
        PaymentStatus status = "SUCCESS".equalsIgnoreCase(request.getDemoPaymentResult()) ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;

        // Build Payment Entity
        Payment payment = Payment.builder()
                .application(application)
                .transactionId(transactionId)
                .amount(premium)
                .paymentMethod(request.getPaymentMethod())
                .status(status)
                .paymentDate(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // Update Application status if payment succeeded
        if (status == PaymentStatus.SUCCESS) {
            application.setStatus(ApplicationStatus.PAYMENT_COMPLETED);
            applicationRepository.save(application);
        }

        return mapToResponse(savedPayment);
    }

    public PaymentResponse getPaymentById(Long id) {
        User user = getAuthenticatedUser();
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new CustomException("Payment not found", HttpStatus.NOT_FOUND));

        // Security check
        if (!payment.getApplication().getUser().getId().equals(user.getId()) && user.getRole() != UserRole.ADMIN) {
            throw new CustomException("Access denied to view this payment", HttpStatus.FORBIDDEN);
        }

        return mapToResponse(payment);
    }

    public PaymentResponse getPaymentByApplication(Long applicationId) {
        User user = getAuthenticatedUser();
        Payment payment = paymentRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new CustomException("Payment not found for this application", HttpStatus.NOT_FOUND));

        // Security check
        if (!payment.getApplication().getUser().getId().equals(user.getId()) && user.getRole() != UserRole.ADMIN) {
            throw new CustomException("Access denied to view this payment", HttpStatus.FORBIDDEN);
        }

        return mapToResponse(payment);
    }

    public List<PaymentResponse> getUserPayments() {
        User user = getAuthenticatedUser();
        return paymentRepository.findAll().stream()
                .filter(p -> p.getApplication().getUser().getId().equals(user.getId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PaymentResponse> getAllPaymentsForAdmin() {
        getAuthenticatedUser(); // Verify authentication
        return paymentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PaymentReceiptResponse generateReceipt(Long paymentId) {
        User user = getAuthenticatedUser();
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException("Payment not found", HttpStatus.NOT_FOUND));

        // Security check
        if (!payment.getApplication().getUser().getId().equals(user.getId()) && user.getRole() != UserRole.ADMIN) {
            throw new CustomException("Access denied to view this receipt", HttpStatus.FORBIDDEN);
        }

        return PaymentReceiptResponse.builder()
                .transactionId(payment.getTransactionId())
                .applicationNumber(payment.getApplication().getApplicationNumber())
                .customerName(payment.getApplication().getTravelDetails().getTravellerName())
                .planName(payment.getApplication().getPlan().getName())
                .amount(payment.getAmount())
                .currency("INR")
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .paymentDate(payment.getPaymentDate())
                .build();
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .applicationId(payment.getApplication().getId())
                .applicationNumber(payment.getApplication().getApplicationNumber())
                .transactionId(payment.getTransactionId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .paymentDate(payment.getPaymentDate())
                .build();
    }
}
