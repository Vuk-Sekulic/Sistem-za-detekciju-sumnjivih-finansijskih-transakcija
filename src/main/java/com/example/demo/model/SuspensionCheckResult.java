package com.example.demo.model;

import java.util.ArrayList;
import java.util.List;

public class SuspensionCheckResult {

    private String userId;
    private boolean suspendAccount;
    private RiskLevel riskLevel;
    private int fraudTransactionsLast7;
    private List<String> reasons = new ArrayList<>();

    public SuspensionCheckResult() {
    }

    public SuspensionCheckResult(
            String userId,
            boolean suspendAccount,
            RiskLevel riskLevel,
            int fraudTransactionsLast7,
            List<String> reasons
    ) {
        this.userId = userId;
        this.suspendAccount = suspendAccount;
        this.riskLevel = riskLevel;
        this.fraudTransactionsLast7 = fraudTransactionsLast7;
        this.reasons = reasons;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isSuspendAccount() {
        return suspendAccount;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public int getFraudTransactionsLast7() {
        return fraudTransactionsLast7;
    }

    public List<String> getReasons() {
        return reasons;
    }
}