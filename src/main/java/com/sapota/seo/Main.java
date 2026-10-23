package com.sapota.seo;

import com.formdev.flatlaf.FlatLightLaf;
import com.sapota.seo.db.DatabaseConnection;
import com.sapota.seo.db.SchemaInitializer;
import com.sapota.seo.model.User;
import com.sapota.seo.service.SchedulerService;
import com.sapota.seo.ui.LoginDialog;
import com.sapota.seo.ui.MainFrame;
import com.sapota.seo.util.Session;

import javax.swing.*;

/**
 * Điểm khởi chạy ứng dụng Dashboard Backlink Gap & Dofollow Checker.
 *
 * Trước khi chạy, cần:
 *  1. Bật MySQL (qua XAMPP Control Panel hoặc MySQL Server).
 *  2. Import file sql/schema.sql để tạo database "sapota_seo".
 *  3. Kiểm tra lại thông tin kết nối trong DatabaseConnection.java.
 *
 * Lần đầu chạy app tự tạo tài khoản quản trị: admin / admin123 (hãy đổi mật khẩu sau khi đăng nhập).
 */
public class Main {

    public static void main(String[] args) {
        // FlatLaf: giao diện phẳng, hiện đại; nếu lỗi thì dùng giao diện hệ điều hành
        try {
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            FlatLightLaf.setup();
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // dùng giao diện Swing mặc định
            }
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

            try {
                SchemaInitializer.init(); // tự tạo bảng outreach, tracked_links, users nếu chưa có
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Không khởi tạo được các bảng bổ sung: " + ex.getMessage(),
                        "Lỗi Database", JOptionPane.ERROR_MESSAGE);
                return;
            }

            SchedulerService.getInstance().restartFromSettings(); // bật lịch kiểm tra tự động nếu đã cấu hình
            showLoginThenMain();
        });
    }

    /** Hiện đăng nhập; đăng nhập xong mở cửa sổ chính. Đăng xuất thì quay lại màn hình đăng nhập. */
    private static void showLoginThenMain() {
        User user = LoginDialog.showLogin();
        if (user == null) {
            System.exit(0); // người dùng bấm Thoát / đóng hộp thoại
            return;
        }
        MainFrame[] holder = new MainFrame[1];
        holder[0] = new MainFrame(() -> {
            MainFrame old = holder[0];
            Session.logout();
            old.setVisible(false);   // ẩn trước, đóng hẳn sau khi đăng nhập lại để JVM không tự thoát giữa chừng
            showLoginThenMain();
            old.dispose();
        });
        holder[0].setVisible(true);
    }
}
