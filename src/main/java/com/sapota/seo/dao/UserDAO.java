package com.sapota.seo.dao;

import com.sapota.seo.db.DatabaseConnection;
import com.sapota.seo.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO cho bảng users (đăng nhập & phân quyền). Luôn dùng PreparedStatement để tránh SQL injection. */
public class UserDAO {

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, username);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm user: " + e.getMessage());
            return null;
        }
    }

    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st = conn.prepareStatement("SELECT * FROM users ORDER BY id");
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách user: " + e.getMessage());
        }
        return list;
    }

    public int countActiveAdmins() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN' AND active = 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    public int countAll() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st = conn.prepareStatement("SELECT COUNT(*) FROM users");
             ResultSet rs = st.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    /** @return true nếu thêm thành công, false nếu trùng username hoặc lỗi. */
    public boolean insert(User u) {
        String sql = "INSERT INTO users (username, password_hash, full_name, role, active) VALUES (?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, u.getUsername());
            st.setString(2, u.getPasswordHash());
            st.setString(3, u.getFullName());
            st.setString(4, u.getRole().name());
            st.setBoolean(5, u.isActive());
            st.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm user: " + e.getMessage());
            return false;
        }
    }

    public void updatePassword(int id, String passwordHash) {
        exec("UPDATE users SET password_hash = ? WHERE id = ?", passwordHash, id);
    }

    public void updateRole(int id, User.Role role) {
        exec("UPDATE users SET role = ? WHERE id = ?", role.name(), id);
    }

    public void updateActive(int id, boolean active) {
        exec("UPDATE users SET active = ? WHERE id = ?", active, id);
    }

    public void delete(int id) {
        exec("DELETE FROM users WHERE id = ?", id);
    }

    private void exec(String sql, Object... params) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) st.setObject(i + 1, params[i]);
            st.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật user: " + e.getMessage());
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setRole(User.Role.fromString(rs.getString("role")));
        u.setActive(rs.getBoolean("active"));
        return u;
    }
}
