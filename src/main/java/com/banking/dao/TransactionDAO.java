package com.banking.dao;

import com.banking.config.DatabaseConfig;
import com.banking.model.Transaction;
import com.banking.model.TransactionStatus;
import com.banking.model.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    private static final Logger logger = LoggerFactory.getLogger(TransactionDAO.class);
    private final DatabaseConfig dbConfig;

    public TransactionDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    public Transaction create(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transaction (account_id, trans_type, amount, balance_after, description, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, transaction.getAccountId());
            stmt.setString(2, transaction.getTransType().name());
            stmt.setBigDecimal(3, transaction.getAmount());
            stmt.setBigDecimal(4, transaction.getBalanceAfter());
            stmt.setString(5, transaction.getDescription());
            stmt.setString(6, transaction.getStatus().name());

            int affected = stmt.executeUpdate();

            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        transaction.setTransactionId(rs.getLong(1));
                        logger.info("Transaction created: ID {}", transaction.getTransactionId());
                    }
                }
            }
            return transaction;
        } catch (SQLException e) {
            logger.error("Error creating transaction", e);
            throw e;
        }
    }

    public void updateStatus(Long transactionId, TransactionStatus status) throws SQLException {
        String sql = "UPDATE transaction SET status = ? WHERE transaction_id = ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setLong(2, transactionId);

            stmt.executeUpdate();
            logger.info("Transaction status updated: ID {} to {}", transactionId, status);
        }
    }

    public List<Transaction> findByAccountId(Long accountId) throws SQLException {
        String sql = "SELECT * FROM transaction WHERE account_id = ? ORDER BY created_at DESC";
        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, accountId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        }
        return transactions;
    }

    public Transaction findById(Long transactionId) throws SQLException {
        String sql = "SELECT * FROM transaction WHERE transaction_id = ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, transactionId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTransaction(rs);
                }
            }
        }
        return null;
    }

    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(rs.getLong("transaction_id"));
        transaction.setAccountId(rs.getLong("account_id"));
        transaction.setTransType(TransactionType.valueOf(rs.getString("trans_type")));
        transaction.setAmount(rs.getBigDecimal("amount"));
        transaction.setBalanceAfter(rs.getBigDecimal("balance_after"));
        transaction.setDescription(rs.getString("description"));
        transaction.setStatus(TransactionStatus.valueOf(rs.getString("status")));
        transaction.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return transaction;
    }
}