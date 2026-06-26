package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;

import com.example.demo.model.EvaluationResult;
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
        result.setFinalRiskLevel(profile.getRiskLevel());

        userRiskProfileService.save(profile);

        return result;
    }

    public EvaluationResult evaluateSequence(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return new EvaluationResult();
        }

        transactions.sort(Comparator.comparing(Transaction::getExecutedAt));

        Transaction currentTransaction = transactions.get(transactions.size() - 1);
        UserRiskProfile profile = userRiskProfileService.getOrCreate(currentTransaction.getUserId());

        LocalDateTime twoMinutesBefore = currentTransaction.getExecutedAt().minusMinutes(2);

        List<Transaction> facts = transactions.stream()
                .filter(t -> t.getUserId().equals(currentTransaction.getUserId()))
                .filter(t -> !t.getExecutedAt().isBefore(twoMinutesBefore))
                .filter(t -> !t.getExecutedAt().isAfter(currentTransaction.getExecutedAt()))
                .toList();

        for (Transaction transaction : facts) {
            transaction.setCurrent(false);
        }

        currentTransaction.setCurrent(true);

        EvaluationResult result = evaluateWithDrools(facts, profile);

        for (Transaction transaction : transactions) {
            transaction.setStatus(result.getStatus());
        }

        transactionRepository.saveAll(transactions);

        updateFraudCountLast7Days(currentTransaction, profile);
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
}