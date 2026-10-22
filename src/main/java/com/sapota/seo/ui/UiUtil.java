package com.sapota.seo.ui;

import javax.swing.*;
import java.awt.Component;
import java.awt.Desktop;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Các hàm tiện ích dùng chung cho giao diện. */
public final class UiUtil {

    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private UiUtil() {
    }

    public static void info(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void warn(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Cảnh báo", JOptionPane.WARNING_MESSAGE);
    }

    public static void error(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Xác nhận",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    /** Mở URL bằng trình duyệt mặc định (nếu thiếu http thì tự thêm https://). */
    public static void openBrowser(Component parent, String url) {
        try {
            String u = url.trim();
            if (!u.startsWith("http://") && !u.startsWith("https://")) {
                u = "https://" + u;
            }
            Desktop.getDesktop().browse(URI.create(u));
        } catch (Exception e) {
            error(parent, "Không mở được trình duyệt: " + e.getMessage());
        }
    }

    /** Chuẩn hoá domain: bỏ http(s)://, www., đường dẫn phía sau và chuyển về chữ thường. */
    public static String cleanDomain(String input) {
        String d = input == null ? "" : input.trim().toLowerCase();
        d = d.replaceFirst("^https?://", "").replaceFirst("^www\\.", "");
        int slash = d.indexOf('/');
        if (slash >= 0) d = d.substring(0, slash);
        return d;
    }

    /** Đọc ngày dạng dd/MM/yyyy hoặc yyyy-MM-dd; rỗng thì trả về null. */
    public static LocalDate parseDate(String s) throws DateTimeParseException {
        if (s == null || s.trim().isEmpty()) return null;
        String t = s.trim();
        if (t.matches("\\d{4}-\\d{2}-\\d{2}")) return LocalDate.parse(t);
        return LocalDate.parse(t, DATE_FMT);
    }

    public static String formatDate(LocalDate d) {
        return d == null ? "" : d.format(DATE_FMT);
    }

    /** Model bảng không cho sửa trực tiếp; types[i] giúp sắp xếp đúng theo số/chữ. */
    public static javax.swing.table.DefaultTableModel newModel(String[] cols, Class<?>[] types) {
        return new javax.swing.table.DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                return types != null && c < types.length ? types[c] : String.class;
            }
        };
    }

    /** Chọn file để lưu; tự thêm đuôi nếu thiếu. Trả về null nếu người dùng huỷ. */
    public static String chooseSavePath(Component parent, String title, String defaultName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        chooser.setSelectedFile(new java.io.File(defaultName));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return null;
        return chooser.getSelectedFile().getAbsolutePath();
    }

    public static int parseIntSafe(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }
}
