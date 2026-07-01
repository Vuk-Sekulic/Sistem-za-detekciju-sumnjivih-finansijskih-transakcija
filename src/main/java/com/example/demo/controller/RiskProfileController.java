package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.UserRiskProfile;
import com.example.demo.service.UserRiskProfileService;

@RestController
@RequestMapping("/api/risk-profiles")
public class RiskProfileController {

    private final UserRiskProfileService userRiskProfileService;

    public RiskProfileController(UserRiskProfileService userRiskProfileService) {
        this.userRiskProfileService = userRiskProfileService;
    }

    @GetMapping("/{userId}")
    public UserRiskProfile getRiskProfile(@PathVariable String userId) {
        return userRiskProfileService.getOrCreate(userId);
    }
}