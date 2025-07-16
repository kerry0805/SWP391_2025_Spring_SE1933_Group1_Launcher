package com.centurionlauncher.auth;

import java.util.List;

public final class AuthContext {

    // Biến static để giữ thể hiện (instance) duy nhất của lớp
    private static AuthContext instance;
    // Các trường để lưu trữ thông tin ngữ cảnh
    private String token;
    private Long userId;
    private String username;
    private String role;

    // Constructor private để ngăn việc tạo đối tượng từ bên ngoài
    private AuthContext() {
    }

    /**
     * Phương thức public, static để lấy thể hiện duy nhất của lớp.
     * Đây là cách duy nhất để truy cập vào đối tượng AuthContext.
     * 
     * @return Thể hiện duy nhất của AuthContext.
     */
    public static AuthContext getInstance() {
        if (instance == null) {
            instance = new AuthContext();
        }
        return instance;
    }

    /**
     * Thiết lập ngữ cảnh sau khi người dùng đăng nhập thành công.
     * 
     * @param token    JWT token nhận được từ backend.
     * @param userId   ID của người dùng.
     * @param username Tên đăng nhập của người dùng.
     * @param roles    Danh sách quyền của người dùng.
     */
    public void setAuthentication(String token, Long userId, String username, String role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    /**
     * Xóa ngữ cảnh xác thực (khi người dùng đăng xuất).
     */
    public void clearAuthentication() {
        this.token = null;
        this.userId = null;
        this.username = null;
        this.role = null;
    }

    // --- Các phương thức Getter để truy xuất thông tin ---

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
     * Kiểm tra xem người dùng đã được xác thực hay chưa.
     * 
     * @return true nếu ngữ cảnh có chứa token, ngược lại là false.
     */
    public boolean isAuthenticated() {
        return this.token != null && !this.token.isEmpty();
    }
}
