package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_risk_profiles")
public class UserRiskProfile {

    @Id
    private String userId;

    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel = RiskLevel.NONE;

    private double typicalTransactionAmount = 500;

    private int fraudTransactionsLast7 = 0;

    public UserRiskProfile() {
    }

    public UserRiskProfile(String userId) {
        this.userId = userId;
        this.riskLevel = RiskLevel.NONE;
        this.typicalTransactionAmount = 500;
        this.fraudTransactionsLast7 = 0;
    }

    public UserRiskProfile(String userId, RiskLevel riskLevel, double typicalTransactionAmount, int fraudTransactionsLast7) {
        this.userId = userId;
        this.riskLevel = riskLevel;
        this.typicalTransactionAmount = typicalTransactionAmount;
        this.fraudTransactionsLast7 = fraudTransactionsLast7;
    }

    public String getUserId() {
        return userId;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public double getTypicalTransactionAmount() {
        return typicalTransactionAmount;
    }

    public int getFraudTransactionsLast7() {
        return fraudTransactionsLast7;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public void setTypicalTransactionAmount(double typicalTransactionAmount) {
        this.typicalTransactionAmount = typicalTransactionAmount;
    }

    public void setFraudTransactionsLast7(int fraudTransactionsLast7) {
        this.fraudTransactionsLast7 = fraudTransactionsLast7;
    }
}