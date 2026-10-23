package com.sapota.seo.db;

import com.sapota.seo.dao.UserDAO;
import com.sapota.seo.model.User;
import com.sapota.seo.util.PasswordHasher;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Tự tạo các bảng bổ sung (outreach, tracked_links, users) nếu chưa có, để người dùng
 * đang có sẵn database cũ không cần import lại schema.sql.
 */
public final class SchemaInitializer {

    private SchemaInitializer() {
    }

    public static void init() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {

            st.executeUpdate("CREATE TABLE IF NOT EXISTS outreach ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "domain VARCHAR(255) NOT NULL, "
                    + "status VARCHAR(30) NOT NULL DEFAULT 'Chưa liên hệ', "
                    + "contact VARCHAR(255), "
                    + "note VARCHAR(1000), "
                    + "last_contact DATE NULL, "
                    + "follow_up DATE NULL, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "UNIQUE KEY uq_outreach_domain (domain)"
                    + ") CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS tracked_links ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "url VARCHAR(1000) NOT NULL, "
                    + "platform VARCHAR(100), "
                    + "placed_date DATE NULL, "
                    + "last_status VARCHAR(30), "
                    + "prev_status VARCHAR(30), "
                    + "last_checked DATETIME NULL, "
                    + "note VARCHAR(500)"
                    + ") CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS users ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "username VARCHAR(50) NOT NULL, "
                    + "password_hash VARCHAR(255) NOT NULL, "
                    + "full_name VARCHAR(100), "
                    + "role VARCHAR(20) NOT NULL DEFAULT 'STAFF', "
                    + "active TINYINT(1) NOT NULL DEFAULT 1, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "UNIQUE KEY uq_users_username (username)"
                    + ") CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }
        seedDefaultAdmin();
    }

    /** Lần chạy đầu tiên (bảng users trống) tạo sẵn tài khoản admin / admin123 — nên đổi mật khẩu ngay sau khi đăng nhập. */
    private static void seedDefaultAdmin() {
        UserDAO dao = new UserDAO();
        if (dao.countAll() == 0) {
            dao.insert(new User("admin", PasswordHasher.hash("admin123"), "Quản trị viên", User.Role.ADMIN));
        }
    }
}
