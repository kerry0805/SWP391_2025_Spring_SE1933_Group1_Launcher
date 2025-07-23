package com.centurionlauncher.auth;

import java.util.List;

public final class AuthContext {

    private static AuthContext instance;
    private String token;
    private Long userId;
    private String username;
    private String role;

    private AuthContext() {
    }

    /**
     * 
     * @return instance of AuthContext
     */
    public static AuthContext getInstance() {
        if (instance == null) {
            instance = new AuthContext();
        }
        return instance;
    }

    /**
     * 
     * @param token    JWT token nhận được từ backend.
     * @param userId   ID của người dùng.
     * @param username Tên đăng nhập của người dùng.
     * @param roles    Danh sách role của người dùng.
     */
    public void setAuthentication(String token, Long userId, String username, String role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.role = role;
    }


    public void clearAuthentication() {
        this.token = null;
        this.userId = null;
        this.username = null;
        this.role = null;
    }


    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRoles() {
        return role;
    }

    /**
     * 
     * @return true if authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        return this.token != null && !this.token.isEmpty();
    }
}
