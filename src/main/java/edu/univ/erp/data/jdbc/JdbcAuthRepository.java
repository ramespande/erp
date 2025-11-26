package edu.univ.erp.data.jdbc;

import edu.univ.erp.data.auth.AuthRecord;
import edu.univ.erp.data.auth.AuthRepository;
import edu.univ.erp.domain.user.Role;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcAuthRepository implements AuthRepository {

    private static final String SELECT_BASE = """
            SELECT user_id, username, role, password_hash, active, last_login, failed_attempts, lockout_until
            FROM auth_users
            """;

    private final DataSource dataSource;

    public JdbcAuthRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<AuthRecord> findByUsername(String username) {
        String sql = SELECT_BASE + " WHERE LOWER(username) = LOWER(?)";
        return queryForSingle(sql, ps -> ps.setString(1, username));
    }

    @Override
    public Optional<AuthRecord> findByUserId(String userId) {
        String sql = SELECT_BASE + " WHERE user_id = ?";
        return queryForSingle(sql, ps -> ps.setString(1, userId));
    }

    @Override
    public void save(AuthRecord record) {
        String sql = """
                INSERT INTO auth_users (user_id, username, role, password_hash, active, last_login, failed_attempts, lockout_until)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    username = VALUES(username),
                    role = VALUES(role),
                    password_hash = VALUES(password_hash),
                    active = VALUES(active),
                    last_login = VALUES(last_login),
                    failed_attempts = VALUES(failed_attempts),
                    lockout_until = VALUES(lockout_until)
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, record.userId());
            ps.setString(2, record.username());
            ps.setString(3, record.role().name());
            ps.setString(4, record.passwordHash());
            ps.setBoolean(5, record.active());
            LocalDateTime lastLogin = record.lastLogin();
            if (lastLogin != null) {
                ps.setTimestamp(6, Timestamp.valueOf(lastLogin));
            } else {
                ps.setTimestamp(6, null);
            }
            ps.setInt(7, record.failedAttempts());
            LocalDateTime lockoutUntil = record.lockoutUntil();
            if (lockoutUntil != null) {
                ps.setTimestamp(8, Timestamp.valueOf(lockoutUntil));
            } else {
                ps.setTimestamp(8, null);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save auth record", e);
        }
    }

    @Override
    public List<AuthRecord> findAll() {
        List<AuthRecord> records = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                records.add(mapRecord(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list auth records", e);
        }
        return records;
    }

    private Optional<AuthRecord> queryForSingle(String sql, SqlConsumer<PreparedStatement> binder) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            binder.accept(ps);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRecord(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to query auth record", e);
        }
    }

    private AuthRecord mapRecord(ResultSet rs) throws SQLException {
        String userId = rs.getString("user_id");
        String username = rs.getString("username");
        Role role = Role.valueOf(rs.getString("role"));
        String passwordHash = rs.getString("password_hash");
        boolean active = rs.getBoolean("active");
        Timestamp lastLoginTs = rs.getTimestamp("last_login");
        LocalDateTime lastLogin = lastLoginTs == null ? null : lastLoginTs.toLocalDateTime();
        int failedAttempts = rs.getInt("failed_attempts");
        Timestamp lockoutUntilTs = rs.getTimestamp("lockout_until");
        LocalDateTime lockoutUntil = lockoutUntilTs == null ? null : lockoutUntilTs.toLocalDateTime();
        return new AuthRecord(userId, username, role, passwordHash, active, lastLogin, failedAttempts, lockoutUntil);
    }

    @FunctionalInterface
    private interface SqlConsumer<T> {
        void accept(T t) throws SQLException;
    }
}

