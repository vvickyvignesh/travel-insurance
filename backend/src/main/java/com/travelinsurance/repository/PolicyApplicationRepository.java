package com.travelinsurance.repository;

import com.travelinsurance.entity.PolicyApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyApplicationRepository extends JpaRepository<PolicyApplication, Long> {
    boolean existsByPlanId(Long planId);
}
