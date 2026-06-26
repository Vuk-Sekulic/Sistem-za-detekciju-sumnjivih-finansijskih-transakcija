package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Transaction;
import com.example.demo.model.TransactionStatus;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserIdOrderByExecutedAtDesc(String userId);

    List<Transaction> findByUserIdAndExecutedAtBetween(
            String userId,
            LocalDateTime from,
            LocalDateTime to
    );

    int countByUserIdAndStatusAndExecutedAtGreaterThanEqual(
            String userId,
            TransactionStatus status,
            LocalDateTime from
    );
}