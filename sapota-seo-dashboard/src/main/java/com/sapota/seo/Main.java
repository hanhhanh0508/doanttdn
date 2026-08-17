package com.sapota.seo;

import com.sapota.seo.db.DatabaseConnection;
import com.sapota.seo.ui.MainFrame;

import javax.swing.*;

/**
 * Điểm khởi chạy ứng dụng Dashboard Backlink Gap & Dofollow Checker.
 *
 * Trước khi chạy, cần:
 *  1. Bật MySQL (qua XAMPP Control Panel hoặc MySQL Server).
 *  2. Import file sql/schema.sql để tạo database "sapota_seo".
 *  3. Kiểm tra lại thông tin kết nối trong DatabaseConnection.java
 *     (mặc định: user=root, password="", phù hợp XAMPP).
 */
public class Main {

    public static void main(String[] args) {
        // Dùng giao diện mặc định của hệ điều hành cho đẹp hơn Swing mặc định
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Nếu không set được thì dùng giao diện Swing mặc định, không sao cả
        }

        SwingUtilities.invokeLater(() -> {
            if (!DatabaseConnection.testConnection()) {
                JOptionPane.showMessageDialog(null,
                        "Không kết nối được tới MySQL (database: sapota_seo).\n\n"
                                + "Vui lòng kiểm tra:\n"
                                + "1. Đã bật MySQL trong XAMPP Control Panel chưa?\n"
                                + "2. Đã import file sql/schema.sql chưa?\n"
                                + "3. Thông tin user/password trong DatabaseConnection.java đã đúng chưa?",
                        "Lỗi kết nối Database",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
