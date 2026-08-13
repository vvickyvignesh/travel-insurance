package com.travelinsurance.repository;

import com.travelinsurance.entity.InsurancePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsurancePlanRepository extends JpaRepository<InsurancePlan, Long> {
    List<InsurancePlan> findByActiveTrue();
    Optional<InsurancePlan> findByIdAndActiveTrue(Long id);
    List<InsurancePlan> findByActiveTrueAndNameContainingIgnoreCase(String keyword);
    List<InsurancePlan> findByNameContainingIgnoreCase(String keyword);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
}
