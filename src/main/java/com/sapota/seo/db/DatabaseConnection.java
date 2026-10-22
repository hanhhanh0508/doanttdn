package com.sapota.seo.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Lớp quản lý kết nối tới MySQL Server / XAMPP (MySQL).
 *
 * Mặc định cấu hình sẵn cho XAMPP: host=localhost, port=3306,
 * user=root, password="" (rỗng), database=sapota_seo.
 *
 * Nếu dùng MySQL Server độc lập, chỉnh lại 4 hằng số bên dưới
 * cho khớp với thông tin đăng nhập của bạn.
 */
public final class DatabaseConnection {

    private static final String HOST = "localhost";
    private static final String PORT = "3307";
    private static final String DATABASE = "sapota_seo";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // XAMPP mặc định không có mật khẩu

    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
            + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";

    private DatabaseConnection() {
        // Không cho khởi tạo instance, chỉ dùng static method
    }

    /**
     * Mở một kết nối mới tới MySQL. Gọi close() sau khi dùng xong
     * (khuyến khích dùng try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Không tìm thấy MySQL JDBC Driver. "
                    + "Kiểm tra lại thư viện mysql-connector-j trong classpath.", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Kiểm tra nhanh xem có kết nối được tới database hay không.
     * Dùng để báo lỗi rõ ràng ngay khi khởi động ứng dụng.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Không kết nối được tới MySQL: " + e.getMessage());
            return false;
        }
    }
}
