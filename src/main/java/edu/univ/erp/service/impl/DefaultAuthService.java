package edu.univ.erp.service.impl;

import edu.univ.erp.api.common.OperationResult;
import edu.univ.erp.auth.PasswordHasher;
import edu.univ.erp.auth.SessionContext;
import edu.univ.erp.data.auth.AuthRecord;
import edu.univ.erp.data.auth.AuthRepository;
import edu.univ.erp.domain.user.Role;
import edu.univ.erp.service.AuthService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public final class DefaultAuthService implements AuthService {

    private final AuthRepository authRepository;
    private final SessionContext sessionContext;

    public DefaultAuthService(AuthRepository authRepository, SessionContext sessionContext) {
        this.authRepository = authRepository;
        this.sessionContext = sessionContext;
    }

    @Override
    public OperationResult<Role> login(String username, String password) {
        return authRepository.findByUsername(username)
                .filter(AuthRecord::active)
                .map(record -> {
                    LocalDateTime now = LocalDateTime.now();
                    
                    // Check if account is locked out
                    if (record.lockoutUntil() != null && record.lockoutUntil().isAfter(now)) {
                        long minutesRemaining = ChronoUnit.MINUTES.between(now, record.lockoutUntil());
                        return OperationResult.<Role>failure("Account locked. Please try again in " + minutesRemaining + " minute(s).");
                    }
                    
                    // If lockout period has expired, reset failed attempts
                    if (record.lockoutUntil() != null && record.lockoutUntil().isBefore(now)) {
                        record = record.withFailedAttempts(0).withLockoutUntil(null);
                        authRepository.save(record);
                    }
                    
                    if (!PasswordHasher.verify(password, record.passwordHash())) {
                        // Increment failed attempts
                        int newAttempts = record.failedAttempts() + 1;
                        
                        if (newAttempts >= 5) {
                            // Lock account for 1 minute
                            LocalDateTime lockoutUntil = now.plusMinutes(1);
                            authRepository.save(record.withFailedAttempts(newAttempts).withLockoutUntil(lockoutUntil));
                            return OperationResult.<Role>failure("Account locked for 1 minute after 5 failed attempts. Please try again later.");
                        } else {
                            authRepository.save(record.withFailedAttempts(newAttempts));
                            int remaining = 5 - newAttempts;
                            return OperationResult.<Role>failure("Incorrect username or password. " + remaining + " attempt(s) remaining.");
                        }
                    }
                    
                    // Successful login - reset failed attempts and update last login
                    sessionContext.establish(record.userId(), record.username(), record.role());
                    authRepository.save(record.withLastLogin(now).withFailedAttempts(0).withLockoutUntil(null));
                    return OperationResult.success(record.role());
                })
                .orElseGet(() -> OperationResult.failure("Incorrect username or password."));
    }

    @Override
    public void logout() {
        sessionContext.clear();
    }

    @Override
    public OperationResult<Void> changePassword(String currentPassword, String newPassword) {
        if (!sessionContext.isLoggedIn()) {
            return OperationResult.failure("Please login first.");
        }

        return authRepository.findByUserId(sessionContext.getUserId())
                .map(record -> {
                    if (!PasswordHasher.verify(currentPassword, record.passwordHash())) {
                        return OperationResult.<Void>failure("Current password is incorrect.");
                    }
                    var updated = record.withPasswordHash(PasswordHasher.hash(newPassword));
                    authRepository.save(updated);
                    return OperationResult.<Void>success(null, "Password updated.");
                })
                .orElseGet(() -> OperationResult.<Void>failure("User record missing."));
    }
}

