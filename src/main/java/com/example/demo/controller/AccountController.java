package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.SuspensionCheckResult;
import com.example.demo.service.SuspensionService;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final SuspensionService suspensionService;

    public AccountController(SuspensionService suspensionService) {
        this.suspensionService = suspensionService;
    }

    @GetMapping("/{userId}/suspension-check")
    public SuspensionCheckResult checkSuspension(@PathVariable String userId) {
        return suspensionService.checkSuspension(userId);
    }
}