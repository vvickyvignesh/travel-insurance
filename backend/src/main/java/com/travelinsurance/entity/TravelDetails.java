package com.travelinsurance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "travel_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Size(max = 100)
    @Column(name = "traveller_name", nullable = false, length = 100)
    private String travellerName;

    @NotNull
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @NotBlank
    @Size(max = 50)
    @Column(name = "passport_number", nullable = false, length = 50)
    private String passportNumber;

    @Size(max = 20)
    @Column(length = 20)
    private String phone;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String destination;

    @NotNull
    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @NotNull
    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;

    @NotBlank
    @Size(max = 50)
    @Column(name = "trip_type", nullable = false, length = 50)
    private String tripType;

    @Size(max = 100)
    @Column(name = "travel_purpose", length = 100)
    private String travelPurpose;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
