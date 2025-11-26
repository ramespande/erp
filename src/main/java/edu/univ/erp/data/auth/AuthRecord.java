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
        int failedAttempts,
        LocalDateTime lockoutUntil) {

    public AuthRecord withPasswordHash(String newHash) {
        return new AuthRecord(userId, username, role, newHash, active, lastLogin, failedAttempts, lockoutUntil);
    }

    public AuthRecord withFailedAttempts(int attempts) {
        return new AuthRecord(userId, username, role, passwordHash, active, lastLogin, attempts, lockoutUntil);
    }

    public AuthRecord withLastLogin(LocalDateTime time) {
        return new AuthRecord(userId, username, role, passwordHash, active, time, failedAttempts, lockoutUntil);
    }

    public AuthRecord withLockoutUntil(LocalDateTime time) {
        return new AuthRecord(userId, username, role, passwordHash, active, lastLogin, failedAttempts, time);
    }
}

