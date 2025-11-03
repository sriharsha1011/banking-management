package com.banking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private Long transactionId;
    private Long accountId;
    private TransactionType transType;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String description;
    private TransactionStatus status;
    private LocalDateTime createdAt;

    public Transaction() {}

    public Transaction(Long accountId, TransactionType transType, BigDecimal amount,
                       BigDecimal balanceAfter, String description) {
        this.accountId = accountId;
        this.transType = transType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.status = TransactionStatus.PENDING;
    }

    // Getters and Setters
    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public TransactionType getTransType() { return transType; }
    public void setTransType(TransactionType transType) { this.transType = transType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", transType=" + transType +
                ", amount=" + amount +
                ", status=" + status +
                '}';
    }
}