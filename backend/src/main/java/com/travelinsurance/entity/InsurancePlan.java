package com.travelinsurance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "insurance_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsurancePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @DecimalMin(value = "0.0")
    @Column(name = "coverage_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal coverageAmount;

    @NotNull
    @DecimalMin(value = "0.0")
    @Column(name = "medical_coverage", nullable = false, precision = 12, scale = 2)
    private BigDecimal medicalCoverage;

    @NotNull
    @DecimalMin(value = "0.0")
    @Column(name = "baggage_coverage", nullable = false, precision = 12, scale = 2)
    private BigDecimal baggageCoverage;

    @NotNull
    @DecimalMin(value = "0.0")
    @Column(name = "trip_cancellation", nullable = false, precision = 12, scale = 2)
    private BigDecimal tripCancellation;

    @Column(name = "emergency_assistance")
    private Boolean emergencyAssistance = true;

    @NotNull
    @DecimalMin(value = "0.0")
    @Column(name = "base_premium", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePremium;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (emergencyAssistance == null) emergencyAssistance = true;
        if (active == null) active = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
