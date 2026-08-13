package com.travelinsurance.controller;

import com.travelinsurance.dto.TravelDetailsRequest;
import com.travelinsurance.dto.TravelDetailsResponse;
import com.travelinsurance.service.TravelDetailsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/travel-details")
public class TravelDetailsController {

    @Autowired
    private TravelDetailsService travelDetailsService;

    @PostMapping
    public ResponseEntity<TravelDetailsResponse> createTravelDetails(@Valid @RequestBody TravelDetailsRequest request) {
        TravelDetailsResponse created = travelDetailsService.createTravelDetails(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TravelDetailsResponse>> getUserTravelDetails() {
        return ResponseEntity.ok(travelDetailsService.getUserTravelDetails());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TravelDetailsResponse> getTravelDetailsById(@PathVariable Long id) {
        return ResponseEntity.ok(travelDetailsService.getTravelDetailsById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TravelDetailsResponse> updateTravelDetails(
            @PathVariable Long id, @Valid @RequestBody TravelDetailsRequest request) {
        return ResponseEntity.ok(travelDetailsService.updateTravelDetails(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTravelDetails(@PathVariable Long id) {
        travelDetailsService.deleteTravelDetails(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Travel details deleted successfully");
        return ResponseEntity.ok(response);
    }
}
