package com.travelinsurance.repository;

import com.travelinsurance.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    boolean existsByPlanId(Long planId);
}
