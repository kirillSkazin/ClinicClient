package org.example.clinic.client.session;

import org.example.clinic.client.model.Role;


public final class Session {

    private static final Session INSTANCE = new Session();

    private String token;
    private Long userId;
    private String username;
    private String fullName;
    private Role role;

    private Session() {
    }

    public static Session get() {
        return INSTANCE;
    }

    public boolean isAuthenticated() {
        return token != null && !token.isBlank();
    }

    public void setUser(String token, Long userId, String username, String fullName, Role role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    public void clear() {
        this.token = null;
        this.userId = null;
        this.username = null;
        this.fullName = null;
        this.role = null;
    }

    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public Role getRole() { return role; }
}
