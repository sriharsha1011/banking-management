package com.banking.service;

import com.banking.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TransactionLogService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionLogService.class);
    private final DatabaseConfig dbConfig;

    public TransactionLogService() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    public void logTransaction(Long transactionId, String logLevel, String message) {
        String sql = "INSERT INTO transaction_log (transaction_id, log_level, message) VALUES (?, ?, ?)";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, transactionId);
            stmt.setString(2, logLevel);
            stmt.setString(3, message);

            stmt.executeUpdate();
            logger.debug("Transaction log created for transaction ID: {}", transactionId);

        } catch (SQLException e) {
            logger.error("Failed to create transaction log", e);
        }
    }

    public void logTransactionWithDetails(Long transactionId, String logLevel,
                                          String message, String ipAddress, String userAgent) {
        String sql = "INSERT INTO transaction_log (transaction_id, log_level, message, ip_address, user_agent) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, transactionId);
            stmt.setString(2, logLevel);
            stmt.setString(3, message);
            stmt.setString(4, ipAddress);
            stmt.setString(5, userAgent);

            stmt.executeUpdate();
            logger.debug("Detailed transaction log created for transaction ID: {}", transactionId);

        } catch (SQLException e) {
            logger.error("Failed to create detailed transaction log", e);
        }
    }
}