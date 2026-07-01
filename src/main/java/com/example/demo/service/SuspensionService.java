package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.springframework.stereotype.Service;

import com.example.demo.model.RiskLevel;
import com.example.demo.model.SuspensionCheckResult;
import com.example.demo.model.TransactionStatus;
import com.example.demo.model.UserRiskProfile;
import com.example.demo.repository.TransactionRepository;

@Service
public class SuspensionService {

    private final KieContainer kieContainer;
    private final TransactionRepository transactionRepository;
    private final UserRiskProfileService userRiskProfileService;

    public SuspensionService(
            TransactionRepository transactionRepository,
            UserRiskProfileService userRiskProfileService
    ) {
        KieServices kieServices = KieServices.Factory.get();
        this.kieContainer = kieServices.getKieClasspathContainer();
        this.transactionRepository = transactionRepository;
        this.userRiskProfileService = userRiskProfileService;
    }

    public SuspensionCheckResult checkSuspension(String userId) {
        UserRiskProfile profile = userRiskProfileService.getOrCreate(userId);

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        int fraudCount = transactionRepository
                .countByUserIdAndStatusAndExecutedAtGreaterThanEqual(
                        userId,
                        TransactionStatus.FRAUD,
                        sevenDaysAgo
                );

        profile.setFraudTransactionsLast7(fraudCount);
        userRiskProfileService.save(profile);

        boolean shouldSuspend = proveSuspensionGoal(profile);

        List<String> reasons = new ArrayList<>();

        if (profile.getRiskLevel() == RiskLevel.HIGH) {
            reasons.add("User has high risk level");
        } else {
            reasons.add("User does not have high risk level");
        }

        if (fraudCount >= 3) {
            reasons.add("User has at least 3 fraud transactions in the last 7 days");
        } else {
            reasons.add("User has fewer than 3 fraud transactions in the last 7 days");
        }

        return new SuspensionCheckResult(
                userId,
                shouldSuspend,
                profile.getRiskLevel(),
                fraudCount,
                reasons
        );
    }

    private boolean proveSuspensionGoal(UserRiskProfile profile) {
        KieSession kieSession = kieContainer.newKieSession("fraudKSession");

        try {
            kieSession.insert(profile);

            QueryResults results = kieSession.getQueryResults("shouldSuspendAccount");

            return results.size() > 0;
        } finally {
            kieSession.dispose();
        }
    }
}