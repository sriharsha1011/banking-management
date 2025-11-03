package com.banking.exception;

public class CustomerNotFoundException extends BankingException {
    public CustomerNotFoundException(Long customerId) {
        super("Customer not found with ID: " + customerId);
    }

    public CustomerNotFoundException(String email) {
        super("Customer not found with email: " + email);
    }
}