package com.banking.exception;

public class InsufficientFundsException extends BankingException {
    private final double requiredAmount;
    private final double availableBalance;

    public InsufficientFundsException(double requiredAmount, double availableBalance) {
        super(String.format("Insufficient funds. Required: %.2f, Available: %.2f",
                requiredAmount, availableBalance));
        this.requiredAmount = requiredAmount;
        this.availableBalance = availableBalance;
    }

    public double getRequiredAmount() {
        return requiredAmount;
    }

    public double getAvailableBalance() {
        return availableBalance;
    }
}