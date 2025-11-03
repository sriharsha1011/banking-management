package com.banking.exception;

public class AccountClosedException extends BankingException {
    public AccountClosedException(String accountNumber) {
        super("Account is closed: " + accountNumber);
    }
}