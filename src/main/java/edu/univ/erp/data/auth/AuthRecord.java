package edu.univ.erp.data.auth;

import edu.univ.erp.domain.user.Role;

import java.time.LocalDateTime;

public record AuthRecord(
        String userId,
        String username,
        Role role,
        String passwordHash,
        boolean active,
        LocalDateTime lastLogin,
        int failedAttempts) {

    public AuthRecord withPasswordHash(String newHash) {
        return new AuthRecord(userId, username, role, newHash, active, lastLogin, failedAttempts);
    }

    public AuthRecord withFailedAttempts(int attempts) {
        return new AuthRecord(userId, username, role, passwordHash, active, lastLogin, attempts);
    }

    public AuthRecord withLastLogin(LocalDateTime time) {
        return new AuthRecord(userId, username, role, passwordHash, active, time, failedAttempts);
    }
}

