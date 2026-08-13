package com.travelinsurance.repository;

import com.travelinsurance.entity.PolicyApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyApplicationRepository extends JpaRepository<PolicyApplication, Long> {
    boolean existsByPlanId(Long planId);
    List<PolicyApplication> findByUserId(Long userId);

    @Query("SELECT MAX(pa.id) FROM PolicyApplication pa")
    Optional<Long> findMaxId();
}
