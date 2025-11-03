package com.banking.service;

import com.banking.dao.AccountDAO;
import com.banking.dao.TransactionDAO;
import com.banking.exception.*;
import com.banking.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class AccountService {
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;
    private final TransactionLogService logService;

    public AccountService() {
        this.accountDAO = new AccountDAO();
        this.transactionDAO = new TransactionDAO();
        this.logService = new TransactionLogService();
    }

    public Account createAccount(Account account) throws BankingException {
        try {
            Account created = accountDAO.create(account);
            logger.info("Account created successfully: {}", created.getAccountNumber());
            return created;
        } catch (SQLException e) {
            logger.error("Failed to create account", e);
            throw new BankingException("Failed to create account", e);
        }
    }

    public Account getAccount(String accountNumber) throws BankingException {
        try {
            return accountDAO.findByAccountNumber(accountNumber);
        } catch (SQLException e) {
            logger.error("Database error while fetching account", e);
            throw new BankingException("Failed to fetch account", e);
        }
    }

    public List<Account> getCustomerAccounts(Long customerId) throws BankingException {
        try {
            return accountDAO.findByCustomerId(customerId);
        } catch (SQLException e) {
            logger.error("Failed to fetch customer accounts", e);
            throw new BankingException("Failed to fetch customer accounts", e);
        }
    }

    public Transaction deposit(String accountNumber, BigDecimal amount, String description)
            throws BankingException {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Deposit amount must be positive");
        }

        try {
            Account account = accountDAO.findByAccountNumber(accountNumber);
            validateAccountActive(account);

            BigDecimal newBalance = account.getBalance().add(amount);

            Transaction transaction = new Transaction(
                    account.getAccountId(),
                    TransactionType.DEPOSIT,
                    amount,
                    newBalance,
                    description
            );

            transaction = transactionDAO.create(transaction);
            accountDAO.updateBalance(account.getAccountId(), newBalance);
            transactionDAO.updateStatus(transaction.getTransactionId(), TransactionStatus.SUCCESS);

            logService.logTransaction(transaction.getTransactionId(),
                    "INFO", "Deposit successful: " + amount);

            logger.info("Deposit completed: {} to account {}", amount, accountNumber);
            return transaction;

        } catch (AccountNotFoundException | AccountClosedException e) {
            throw e;
        } catch (SQLException e) {
            logger.error("Deposit failed", e);
            throw new BankingException("Deposit transaction failed", e);
        }
    }

    public Transaction withdraw(String accountNumber, BigDecimal amount, String description)
            throws BankingException {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Withdrawal amount must be positive");
        }

        try {
            Account account = accountDAO.findByAccountNumber(accountNumber);
            validateAccountActive(account);

            if (account.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException(
                        amount.doubleValue(),
                        account.getBalance().doubleValue()
                );
            }

            BigDecimal newBalance = account.getBalance().subtract(amount);

            Transaction transaction = new Transaction(
                    account.getAccountId(),
                    TransactionType.WITHDRAWAL,
                    amount,
                    newBalance,
                    description
            );

            transaction = transactionDAO.create(transaction);
            accountDAO.updateBalance(account.getAccountId(), newBalance);
            transactionDAO.updateStatus(transaction.getTransactionId(), TransactionStatus.SUCCESS);

            logService.logTransaction(transaction.getTransactionId(),
                    "INFO", "Withdrawal successful: " + amount);

            logger.info("Withdrawal completed: {} from account {}", amount, accountNumber);
            return transaction;

        } catch (AccountNotFoundException | AccountClosedException | InsufficientFundsException e) {
            throw e;
        } catch (SQLException e) {
            logger.error("Withdrawal failed", e);
            throw new BankingException("Withdrawal transaction failed", e);
        }
    }

    public void transfer(String fromAccountNumber, String toAccountNumber,
                         BigDecimal amount, String description) throws BankingException {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Transfer amount must be positive");
        }

        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new InvalidTransactionException("Cannot transfer to the same account");
        }

        try {
            Account fromAccount = accountDAO.findByAccountNumber(fromAccountNumber);
            Account toAccount = accountDAO.findByAccountNumber(toAccountNumber);

            validateAccountActive(fromAccount);
            validateAccountActive(toAccount);

            if (fromAccount.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException(
                        amount.doubleValue(),
                        fromAccount.getBalance().doubleValue()
                );
            }

            // Debit from source
            withdraw(fromAccountNumber, amount, "Transfer to " + toAccountNumber + ": " + description);

            // Credit to destination
            deposit(toAccountNumber, amount, "Transfer from " + fromAccountNumber + ": " + description);

            logger.info("Transfer completed: {} from {} to {}", amount, fromAccountNumber, toAccountNumber);

        } catch (AccountNotFoundException | AccountClosedException | InsufficientFundsException e) {
            throw e;
        } catch (BankingException | SQLException e) {
            logger.error("Transfer failed", e);
            throw new BankingException("Transfer transaction failed", e);
        }
    }

    public BigDecimal getBalance(String accountNumber) throws BankingException {
        Account account = getAccount(accountNumber);
        return account.getBalance();
    }

    public List<Transaction> getTransactionHistory(String accountNumber) throws BankingException {
        try {
            Account account = accountDAO.findByAccountNumber(accountNumber);
            return transactionDAO.findByAccountId(account.getAccountId());
        } catch (SQLException e) {
            logger.error("Failed to fetch transaction history", e);
            throw new BankingException("Failed to fetch transaction history", e);
        }
    }

    public void closeAccount(String accountNumber) throws BankingException {
        try {
            Account account = accountDAO.findByAccountNumber(accountNumber);

            if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
                throw new InvalidTransactionException(
                        "Cannot close account with non-zero balance: " + account.getBalance()
                );
            }

            accountDAO.updateStatus(account.getAccountId(), AccountStatus.CLOSED);
            logger.info("Account closed: {}", accountNumber);

        } catch (SQLException e) {
            logger.error("Failed to close account", e);
            throw new BankingException("Failed to close account", e);
        }
    }

    private void validateAccountActive(Account account) throws AccountClosedException {
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountClosedException(account.getAccountNumber());
        }
    }
}