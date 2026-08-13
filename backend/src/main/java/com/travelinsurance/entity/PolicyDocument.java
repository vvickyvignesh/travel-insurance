package com.travelinsurance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "policy_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", unique = true, nullable = false)
    private Policy policy;

    @NotBlank
    @Size(max = 255)
    @Column(name = "document_name", nullable = false, length = 255)
    private String documentName;

    @NotBlank
    @Size(max = 255)
    @Column(name = "document_path", nullable = false, length = 255)
    private String documentPath;

    @NotBlank
    @Size(max = 50)
    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
