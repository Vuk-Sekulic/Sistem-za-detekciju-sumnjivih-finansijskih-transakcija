package com.example.demo.model;

public class RiskDecreaseRequest {

    private String userId;
    private boolean applied = false;

    public RiskDecreaseRequest() {
    }

    public RiskDecreaseRequest(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isApplied() {
        return applied;
    }

    public void setApplied(boolean applied) {
        this.applied = applied;
    }
}