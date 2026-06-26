package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.UserRiskProfile;

public interface UserRiskProfileRepository extends JpaRepository<UserRiskProfile, String> {
}