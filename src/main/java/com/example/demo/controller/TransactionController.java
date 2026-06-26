package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;

import com.example.demo.model.EvaluationResult;
import com.example.demo.model.Transaction;
import com.example.demo.service.FraudDetectionService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final FraudDetectionService fraudDetectionService;

    public TransactionController(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }

    @PostMapping("/evaluate")
    public EvaluationResult evaluate(@RequestBody Transaction transaction) {
        return fraudDetectionService.evaluate(transaction);
    }
}