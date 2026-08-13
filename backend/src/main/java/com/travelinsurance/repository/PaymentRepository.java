package com.travelinsurance.repository;

import com.travelinsurance.entity.Payment;
import com.travelinsurance.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByApplicationId(Long applicationId);
    Optional<Payment> findByTransactionId(String transactionId);
    boolean existsByApplicationIdAndStatus(Long applicationId, PaymentStatus status);
}
