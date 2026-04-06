package com.pconk.procurement.infrastructure.config;

import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class UserContext {
    private String userId;
    private String username;
    private String role;
    private String token;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isAuthenticated() {
        return userId != null;
    }

    @Override
    public String toString() {
        return "UserContext{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}