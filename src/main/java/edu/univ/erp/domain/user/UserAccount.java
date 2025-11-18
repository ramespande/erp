package edu.univ.erp.domain.user;

import java.time.LocalDateTime;
import java.util.Objects;

public final class UserAccount {
    private final String userId;
    private final String username;
    private final Role role;
    private final boolean active;
    private final LocalDateTime lastLogin;

    public UserAccount(String userId, String username, Role role, boolean active, LocalDateTime lastLogin) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.active = active;
        this.lastLogin = lastLogin;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserAccount that)) {
            return false;
        }
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}

