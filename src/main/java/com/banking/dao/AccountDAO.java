package com.banking.dao;

import com.banking.config.DatabaseConfig;
import com.banking.exception.AccountNotFoundException;
import com.banking.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {
    private static final Logger logger = LoggerFactory.getLogger(AccountDAO.class);
    private final DatabaseConfig dbConfig;

    public AccountDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    public Account create(Account account) throws SQLException {
        String sql = "INSERT INTO account (account_number, customer_id, account_type, balance, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, account.getAccountNumber());
            stmt.setLong(2, account.getCustomerId());
            stmt.setString(3, account.getAccountType().name());
            stmt.setBigDecimal(4, account.getBalance());
            stmt.setString(5, account.getStatus().name());

            int affected = stmt.executeUpdate();

            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        account.setAccountId(rs.getLong(1));
                        logger.info("Account created: {}", account.getAccountNumber());
                    }
                }
            }
            return account;
        } catch (SQLException e) {
            logger.error("Error creating account: {}", account.getAccountNumber(), e);
            throw e;
        }
    }

    public Account findByAccountNumber(String accountNumber) throws SQLException, AccountNotFoundException {
        String sql = "SELECT * FROM account WHERE account_number = ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAccount(rs);
                } else {
                    throw new AccountNotFoundException(accountNumber);
                }
            }
        }
    }

    public Account findById(Long accountId) throws SQLException, AccountNotFoundException {
        String sql = "SELECT * FROM account WHERE account_id = ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, accountId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAccount(rs);
                } else {
                    throw new AccountNotFoundException(accountId);
                }
            }
        }
    }

    public List<Account> findByCustomerId(Long customerId) throws SQLException {
        String sql = "SELECT * FROM account WHERE customer_id = ?";
        List<Account> accounts = new ArrayList<>();

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, customerId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapResultSetToAccount(rs));
                }
            }
        }
        return accounts;
    }

    public void updateBalance(Long accountId, BigDecimal newBalance) throws SQLException {
        String sql = "UPDATE account SET balance = ?, updated_at = CURRENT_TIMESTAMP WHERE account_id = ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, newBalance);
            stmt.setLong(2, accountId);

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                logger.info("Balance updated for account ID: {}, New balance: {}", accountId, newBalance);
            }
        } catch (SQLException e) {
            logger.error("Error updating balance for account ID: {}", accountId, e);
            throw e;
        }
    }

    public void updateStatus(Long accountId, AccountStatus status) throws SQLException {
        String sql = "UPDATE account SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE account_id = ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setLong(2, accountId);

            stmt.executeUpdate();
            logger.info("Status updated for account ID: {} to {}", accountId, status);
        }
    }

    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setAccountId(rs.getLong("account_id"));
        account.setAccountNumber(rs.getString("account_number"));
        account.setCustomerId(rs.getLong("customer_id"));
        account.setAccountType(AccountType.valueOf(rs.getString("account_type")));
        account.setBalance(rs.getBigDecimal("balance"));
        account.setStatus(AccountStatus.valueOf(rs.getString("status")));
        account.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        account.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return account;
    }
}