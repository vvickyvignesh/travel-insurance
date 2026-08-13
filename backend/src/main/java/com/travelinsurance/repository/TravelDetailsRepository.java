package com.travelinsurance.repository;

import com.travelinsurance.entity.TravelDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelDetailsRepository extends JpaRepository<TravelDetails, Long> {
    List<TravelDetails> findByUserId(Long userId);
}
