package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.model.RiskLevel;
import com.example.demo.model.UserRiskProfile;
import com.example.demo.repository.UserRiskProfileRepository;

@Service
public class UserRiskProfileService {

    private final UserRiskProfileRepository userRiskProfileRepository;

    public UserRiskProfileService(UserRiskProfileRepository userRiskProfileRepository) {
        this.userRiskProfileRepository = userRiskProfileRepository;
    }

    public UserRiskProfile getOrCreate(String userId) {
        return userRiskProfileRepository
                .findById(userId)
                .orElseGet(() -> new UserRiskProfile(userId, RiskLevel.NONE, 500, 0));
    }

    public UserRiskProfile save(UserRiskProfile profile) {
        return userRiskProfileRepository.save(profile);
    }
}