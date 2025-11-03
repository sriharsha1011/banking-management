package com.banking;

import com.banking.exception.BankingException;
import com.banking.model.*;
import com.banking.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class BankingApplication {
    private static final Logger logger = LoggerFactory.getLogger(BankingApplication.class);
    private static final AccountService accountService = new AccountService();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        logger.info("Banking Management System started");

        while (true) {
            displayMenu();
            int choice = getIntInput("Enter your choice: ");

            try {
                switch (choice) {
                    case 1 -> createAccount();
                    case 2 -> deposit();
                    case 3 -> withdraw();
                    case 4 -> transfer();
                    case 5 -> checkBalance();
                    case 6 -> viewTransactionHistory();
                    case 7 -> closeAccount();
                    case 8 -> {
                        System.out.println("Thank you for using Banking Management System!");
                        logger.info("Application terminated by user");
                        return;
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            } catch (BankingException e) {
                System.err.println("Error: " + e.getMessage());
                logger.error("Operation failed", e);
            } catch (Exception e) {
                System.err.println("Unexpected error: " + e.getMessage());
                logger.error("Unexpected error", e);
            }

            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }

    private static void displayMenu() {
        System.out.println("\n========================================");
        System.out.println("   BANKING MANAGEMENT SYSTEM");
        System.out.println("========================================");
        System.out.println("1. Create Account");
        System.out.println("2. Deposit");
        System.out.println("3. Withdraw");
        System.out.println("4. Transfer");
        System.out.println("5. Check Balance");
        System.out.println("6. View Transaction History");
        System.out.println("7. Close Account");
        System.out.println("8. Exit");
        System.out.println("========================================");
    }

    private static void createAccount() throws BankingException {
        System.out.println("\n--- Create New Account ---");

        String accountNumber = getStringInput("Enter account number: ");
        Long customerId = getLongInput("Enter customer ID: ");

        System.out.println("Account Types: 1-SAVINGS, 2-CHECKING, 3-FIXED_DEPOSIT");
        int typeChoice = getIntInput("Select account type: ");
        AccountType accountType = switch (typeChoice) {
            case 1 -> AccountType.SAVINGS;
            case 2 -> AccountType.CHECKING;
            case 3 -> AccountType.FIXED_DEPOSIT;
            default -> AccountType.SAVINGS;
        };

        BigDecimal initialDeposit = getBigDecimalInput("Enter initial deposit amount: ");

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setCustomerId(customerId);
        account.setAccountType(accountType);
        account.setBalance(initialDeposit);
        account.setStatus(AccountStatus.ACTIVE);

        Account created = accountService.createAccount(account);
        System.out.println("✓ Account created successfully!");
        System.out.println("Account Number: " + created.getAccountNumber());
        System.out.println("Balance: $" + created.getBalance());
    }

    private static void deposit() throws BankingException {
        System.out.println("\n--- Deposit ---");

        String accountNumber = getStringInput("Enter account number: ");
        BigDecimal amount = getBigDecimalInput("Enter deposit amount: ");
        String description = getStringInput("Enter description (optional): ");

        Transaction transaction = accountService.deposit(accountNumber, amount, description);

        System.out.println("✓ Deposit successful!");
        System.out.println("Transaction ID: " + transaction.getTransactionId());
        System.out.println("Amount: $" + transaction.getAmount());
        System.out.println("Balance After: $" + transaction.getBalanceAfter());
    }

    private static void withdraw() throws BankingException {
        System.out.println("\n--- Withdraw ---");

        String accountNumber = getStringInput("Enter account number: ");
        BigDecimal amount = getBigDecimalInput("Enter withdrawal amount: ");
        String description = getStringInput("Enter description (optional): ");

        Transaction transaction = accountService.withdraw(accountNumber, amount, description);

        System.out.println("✓ Withdrawal successful!");
        System.out.println("Transaction ID: " + transaction.getTransactionId());
        System.out.println("Amount: $" + transaction.getAmount());
        System.out.println("Balance After: $" + transaction.getBalanceAfter());
    }

    private static void transfer() throws BankingException {
        System.out.println("\n--- Transfer ---");

        String fromAccount = getStringInput("Enter source account number: ");
        String toAccount = getStringInput("Enter destination account number: ");
        BigDecimal amount = getBigDecimalInput("Enter transfer amount: ");
        String description = getStringInput("Enter description (optional): ");

        accountService.transfer(fromAccount, toAccount, amount, description);

        System.out.println("✓ Transfer successful!");
        System.out.println("Amount: $" + amount);
        System.out.println("From: " + fromAccount);
        System.out.println("To: " + toAccount);
    }

    private static void checkBalance() throws BankingException {
        System.out.println("\n--- Check Balance ---");

        String accountNumber = getStringInput("Enter account number: ");
        BigDecimal balance = accountService.getBalance(accountNumber);

        System.out.println("Account: " + accountNumber);
        System.out.println("Current Balance: $" + balance);
    }

    private static void viewTransactionHistory() throws BankingException {
        System.out.println("\n--- Transaction History ---");

        String accountNumber = getStringInput("Enter account number: ");
        List<Transaction> transactions = accountService.getTransactionHistory(accountNumber);

        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
        } else {
            System.out.println("\n" + String.format("%-12s %-15s %-12s %-15s %-10s %-20s",
                    "Trans ID", "Type", "Amount", "Balance After", "Status", "Date"));
            System.out.println("-".repeat(90));

            for (Transaction t : transactions) {
                System.out.println(String.format("%-12d %-15s $%-11.2f $%-14.2f %-10s %s",
                        t.getTransactionId(),
                        t.getTransType(),
                        t.getAmount(),
                        t.getBalanceAfter(),
                        t.getStatus(),
                        t.getCreatedAt()));
            }
        }
    }

    private static void closeAccount() throws BankingException {
        System.out.println("\n--- Close Account ---");

        String accountNumber = getStringInput("Enter account number: ");
        String confirm = getStringInput("Are you sure you want to close this account? (yes/no): ");

        if (confirm.equalsIgnoreCase("yes")) {
            accountService.closeAccount(accountNumber);
            System.out.println("✓ Account closed successfully!");
        } else {
            System.out.println("Account closure cancelled.");
        }
    }

    private static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                int value = Integer.parseInt(scanner.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    private static Long getLongInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                long value = Long.parseLong(scanner.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    private static BigDecimal getBigDecimalInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                BigDecimal value = new BigDecimal(scanner.nextLine().trim());
                if (value.compareTo(BigDecimal.ZERO) < 0) {
                    System.out.println("Amount cannot be negative.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid amount.");
            }
        }
    }
}