package com.example.demo.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class EvaluationResult {

    private TransactionStatus status = TransactionStatus.LEGITIMATE;
    private Recommendation recommendation = Recommendation.NONE;
    private RiskLevel finalRiskLevel = RiskLevel.NONE;
    private List<String> activatedRules = new ArrayList<>();

    @JsonIgnore
    private boolean largeAmountDetected = false;

    @JsonIgnore
    private boolean rapidSequenceDetected = false;

    @JsonIgnore
    private boolean locationChangeDetected = false;

    @JsonIgnore
    private boolean unusualAmountDetected = false;

    @JsonIgnore
    private boolean riskUpdated = false;

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public Recommendation getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(Recommendation recommendation) {
        this.recommendation = recommendation;
    }

    public RiskLevel getFinalRiskLevel() {
        return finalRiskLevel;
    }

    public void setFinalRiskLevel(RiskLevel finalRiskLevel) {
        this.finalRiskLevel = finalRiskLevel;
    }

    public List<String> getActivatedRules() {
        return activatedRules;
    }

    public void setActivatedRules(List<String> activatedRules) {
        this.activatedRules = activatedRules;
    }

    public boolean isLargeAmountDetected() {
        return largeAmountDetected;
    }

    public boolean isRapidSequenceDetected() {
        return rapidSequenceDetected;
    }

    public boolean isLocationChangeDetected() {
        return locationChangeDetected;
    }

    public boolean isUnusualAmountDetected() {
        return unusualAmountDetected;
    }

    public boolean isRiskUpdated() {
        return riskUpdated;
    }

    public void setRiskUpdated(boolean riskUpdated) {
        this.riskUpdated = riskUpdated;
    }

    public int getSuspiciousConditionCount() {
        int count = 0;

        if (largeAmountDetected) {
            count++;
        }

        if (rapidSequenceDetected) {
            count++;
        }

        if (locationChangeDetected) {
            count++;
        }

        if (unusualAmountDetected) {
            count++;
        }

        return count;
    }

    public void addActivatedRule(String rule) {
        if (!activatedRules.contains(rule)) {
            activatedRules.add(rule);
        }
    }

    private void markSuspicious(String rule) {
        if (status == TransactionStatus.LEGITIMATE) {
            status = TransactionStatus.SUSPICIOUS;
        }

        if (recommendation == Recommendation.NONE) {
            recommendation = Recommendation.ADDITIONAL_VERIFICATION;
        }

        addActivatedRule(rule);
    }

    public void markLargeAmountDetected() {
        if (!largeAmountDetected) {
            largeAmountDetected = true;
            markSuspicious("Transaction amount is greater than defined threshold");
        }
    }

    public void markRapidSequenceDetected() {
        if (!rapidSequenceDetected) {
            rapidSequenceDetected = true;
            markSuspicious("More than 2 transactions occurred in a short time window");
        }
    }

    public void markLocationChangeDetected() {
        if (!locationChangeDetected) {
            locationChangeDetected = true;
            markSuspicious("Transactions from different locations occurred in a short time window");
        }
    }

    public void markUnusualAmountDetected() {
        if (!unusualAmountDetected) {
            unusualAmountDetected = true;
            markSuspicious("Transaction amount significantly differs from user's average amount");
        }
    }

    public void markFraud(String rule) {
        status = TransactionStatus.FRAUD;
        recommendation = Recommendation.BLOCK_TRANSACTION;
        addActivatedRule(rule);
    }
}