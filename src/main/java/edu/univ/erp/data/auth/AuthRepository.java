package edu.univ.erp.data.auth;

import java.util.List;
import java.util.Optional;

public interface AuthRepository {
    Optional<AuthRecord> findByUsername(String username);

    Optional<AuthRecord> findByUserId(String userId);

    void save(AuthRecord record);

    List<AuthRecord> findAll();
}

