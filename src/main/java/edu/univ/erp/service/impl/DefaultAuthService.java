package edu.univ.erp.service.impl;

import edu.univ.erp.api.common.OperationResult;
import edu.univ.erp.auth.PasswordHasher;
import edu.univ.erp.auth.SessionContext;
import edu.univ.erp.data.auth.AuthRecord;
import edu.univ.erp.data.auth.AuthRepository;
import edu.univ.erp.domain.user.Role;
import edu.univ.erp.service.AuthService;

import java.time.LocalDateTime;

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
                    if (!PasswordHasher.verify(password, record.passwordHash())) {
                        return OperationResult.<Role>failure("Incorrect username or password.");
                    }
                    sessionContext.establish(record.userId(), record.username(), record.role());
                    authRepository.save(record.withLastLogin(LocalDateTime.now()));
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

