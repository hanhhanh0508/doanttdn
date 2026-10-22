package com.sapota.seo.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Tự tạo các bảng bổ sung (outreach, tracked_links) nếu chưa có, để người dùng
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
        }
    }
}
