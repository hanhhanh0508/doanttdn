package com.sapota.seo.model;

/** Tài khoản đăng nhập (bảng users). Vai trò: ADMIN (toàn quyền) hoặc STAFF (nhân viên SEO). */
public class User {

    public enum Role {
        ADMIN("Quản trị viên"),
        STAFF("Nhân viên");

        private final String label;

        Role(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        /** Đọc vai trò từ CSDL; giá trị lạ thì hạ xuống quyền thấp nhất (STAFF) cho an toàn. */
        public static Role fromString(String s) {
            try {
                return Role.valueOf(s == null ? "" : s.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return STAFF;
            }
        }
    }

    private int id;
    private String username;
    private String passwordHash;
    private String fullName;
    private Role role = Role.STAFF;
    private boolean active = true;

    public User() {
    }

    public User(String username, String passwordHash, String fullName, Role role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    /** Tên hiển thị: ưu tiên họ tên, không có thì dùng username. */
    public String getDisplayName() {
        return fullName == null || fullName.isBlank() ? username : fullName;
    }
}
