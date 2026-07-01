package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;

import com.example.demo.model.EvaluationResult;
import com.example.demo.model.RiskDecreaseRequest;
import com.example.demo.model.Transaction;
import com.example.demo.model.TransactionStatus;
import com.example.demo.model.UserRiskProfile;
import com.example.demo.repository.TransactionRepository;

@Service
public class FraudDetectionService {

    private final KieContainer kieContainer;
    private final TransactionRepository transactionRepository;
    private final UserRiskProfileService userRiskProfileService;

    public FraudDetectionService(
            TransactionRepository transactionRepository,
            UserRiskProfileService userRiskProfileService
    ) {
        KieServices kieServices = KieServices.Factory.get();
        this.kieContainer = kieServices.getKieClasspathContainer();
        this.transactionRepository = transactionRepository;
        this.userRiskProfileService = userRiskProfileService;
    }

    public EvaluationResult evaluate(Transaction transaction) {
        UserRiskProfile profile = userRiskProfileService.getOrCreate(transaction.getUserId());

        double typicalAmount = calculateMedianTransactionAmount(transaction.getUserId());
        profile.setTypicalTransactionAmount(typicalAmount);

        LocalDateTime twoMinutesBefore = transaction.getExecutedAt().minusMinutes(2);

        List<Transaction> recentTransactions = transactionRepository
                .findByUserIdAndExecutedAtBetween(
                        transaction.getUserId(),
                        twoMinutesBefore,
                        transaction.getExecutedAt()
                );

        for (Transaction oldTransaction : recentTransactions) {
            oldTransaction.setCurrent(false);
        }

        transaction.setCurrent(true);

        List<Transaction> facts = new ArrayList<>(recentTransactions);
        facts.add(transaction);

        EvaluationResult result = evaluateWithDrools(facts, profile);

        transaction.setStatus(result.getStatus());
        transactionRepository.save(transaction);

        updateFraudCountLast7Days(transaction, profile);

        applyRiskDecreaseIfAllowed(transaction, profile, result);

        result.setFinalRiskLevel(profile.getRiskLevel());

        userRiskProfileService.save(profile);

        return result;
    }

    private EvaluationResult evaluateWithDrools(List<Transaction> transactions, UserRiskProfile profile) {
        KieSession kieSession = kieContainer.newKieSession("fraudKSession");

        EvaluationResult result = new EvaluationResult();

        try {
            kieSession.insert(profile);
            kieSession.insert(result);

            for (Transaction transaction : transactions) {
                kieSession.insert(transaction);
            }

            kieSession.fireAllRules();

            return result;
        } finally {
            kieSession.dispose();
        }
    }

    private void applyRiskDecreaseIfAllowed(
            Transaction transaction,
            UserRiskProfile profile,
            EvaluationResult result
    ) {
        if (result.getStatus() != TransactionStatus.LEGITIMATE) {
            return;
        }

        List<Transaction> userTransactions = transactionRepository
                .findByUserIdOrderByExecutedAtDesc(transaction.getUserId());

        int consecutiveLegitimateTransactions = 0;

        for (Transaction userTransaction : userTransactions) {
            if (userTransaction.getStatus() == TransactionStatus.LEGITIMATE) {
                consecutiveLegitimateTransactions++;
            } else {
                break;
            }
        }

        if (consecutiveLegitimateTransactions < 5) {
            return;
        }

        if (consecutiveLegitimateTransactions % 5 != 0) {
            return;
        }

        applyRiskDecreaseWithDrools(profile, result);
    }
    
    private void applyRiskDecreaseWithDrools(UserRiskProfile profile, EvaluationResult result) {
        KieSession kieSession = kieContainer.newKieSession("fraudKSession");

        RiskDecreaseRequest request = new RiskDecreaseRequest(profile.getUserId());

        try {
            kieSession.insert(profile);
            kieSession.insert(result);
            kieSession.insert(request);

            kieSession.fireAllRules();
        } finally {
            kieSession.dispose();
        }
    }

    private void updateFraudCountLast7Days(Transaction transaction, UserRiskProfile profile) {
        LocalDateTime sevenDaysAgo = transaction.getExecutedAt().minusDays(7);

        int fraudCount = transactionRepository
                .countByUserIdAndStatusAndExecutedAtGreaterThanEqual(
                        transaction.getUserId(),
                        TransactionStatus.FRAUD,
                        sevenDaysAgo
                );

        profile.setFraudTransactionsLast7(fraudCount);
    }

    private double calculateMedianTransactionAmount(String userId) {
        List<Transaction> userTransactions = transactionRepository
                .findByUserIdOrderByExecutedAtDesc(userId);

        if (userTransactions.size() < 3) {
            return 500;
        }

        List<Double> amounts = userTransactions.stream()
                .map(Transaction::getAmount)
                .sorted()
                .toList();

        int size = amounts.size();

        if (size % 2 == 1) {
            return amounts.get(size / 2);
        }

        return (amounts.get(size / 2 - 1) + amounts.get(size / 2)) / 2.0;
    }
}