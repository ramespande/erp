package edu.univ.erp.service;

import edu.univ.erp.api.common.OperationResult;
import edu.univ.erp.domain.user.Role;

public interface AuthService {
    OperationResult<Role> login(String username, String password);

    void logout();

    OperationResult<Void> changePassword(String currentPassword, String newPassword);
}

