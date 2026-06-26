package com.example.demo.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private double amount;
    private String location;
    private String type;

    private LocalDateTime executedAt;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status = TransactionStatus.LEGITIMATE;

    @Transient
    private boolean current;

    public Transaction() {
    }

    public Transaction(String userId, double amount, String location, String type, LocalDateTime executedAt) {
        this.userId = userId;
        this.amount = amount;
        this.location = location;
        this.type = type;
        this.executedAt = executedAt;
    }

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public double getAmount() {
        return amount;
    }

    public String getLocation() {
        return location;
    }

    public String getType() {
        return type;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public boolean isCurrent() {
        return current;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }
}