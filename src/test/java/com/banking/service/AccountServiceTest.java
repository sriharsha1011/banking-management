package com.banking.service;

import com.banking.exception.*;
import com.banking.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountServiceTest {

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService();
    }

    @Test
    void testDepositWithNegativeAmount() {
        BigDecimal negativeAmount = new BigDecimal("-100.00");

        InvalidTransactionException exception = assertThrows(
                InvalidTransactionException.class,
                () -> accountService.deposit("ACC1001", negativeAmount, "Invalid deposit")
        );

        assertTrue(exception.getMessage().contains("must be positive"));
    }

    @Test
    void testTransferToSameAccount() {
        InvalidTransactionException exception = assertThrows(
                InvalidTransactionException.class,
                () -> accountService.transfer("ACC1001", "ACC1001",
                        new BigDecimal("100.00"), "Self transfer")
        );

        assertTrue(exception.getMessage().contains("same account"));
    }
}