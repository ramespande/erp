package edu.univ.erp.auth;

import edu.univ.erp.domain.user.Role;

public final class SessionContext {
    private String userId;
    private String username;
    private Role role;

    public boolean isLoggedIn() {
        return userId != null;
    }

    public void establish(String userId, String username, Role role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public void clear() {
        userId = null;
        username = null;
        role = null;
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
}

