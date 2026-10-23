package com.sapota.seo.ui;

import com.sapota.seo.model.User;
import com.sapota.seo.util.Session;

import javax.swing.*;
import java.awt.*;

/**
 * Cửa sổ chính của ứng dụng SapotaCorp SEO Dashboard.
 * Tab nào cần dữ liệu mới (Refreshable) sẽ tự nạp lại khi được chọn.
 * Phân quyền: tab Cài đặt và Người dùng chỉ hiển thị với Quản trị viên.
 */
public class MainFrame extends JFrame {

    /** @param onLogout chạy khi bấm "Đăng xuất" (Main sẽ đóng cửa sổ này và hiện lại màn hình đăng nhập). */
    public MainFrame(Runnable onLogout) {
        super("SapotaCorp - SEO Dashboard (Backlink Gap, Dofollow Checker, Outreach, Monitor)");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 740);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 540));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.addTab("Dashboard", new DashboardPanel());
        tabbedPane.addTab("Thống kê", new StatsPanel());
        tabbedPane.addTab("Backlink hiện có", new BacklinkPanel());
        tabbedPane.addTab("Outreach Tracker", new OutreachPanel());
        tabbedPane.addTab("Dofollow Checker", new CheckerPanel());
        tabbedPane.addTab("Backlink Monitor", new MonitorPanel());
        tabbedPane.addTab("Lịch sử kiểm tra", new HistoryPanel());
        tabbedPane.addTab("Tạo bài Post (AI)", new PostGeneratorPanel());
        if (Session.isAdmin()) {
            tabbedPane.addTab("Cài đặt", new SettingsPanel());
            tabbedPane.addTab("Người dùng", new UserPanel());
        }

        // Chuyển tab thì nạp lại dữ liệu để các tab luôn đồng bộ với nhau
        tabbedPane.addChangeListener(e -> {
            Component c = tabbedPane.getSelectedComponent();
            if (c instanceof Refreshable) ((Refreshable) c).refresh();
        });

        add(buildUserBar(onLogout), BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JComponent buildUserBar(Runnable onLogout) {
        User u = Session.getCurrentUser();
        String who = u == null ? "" : "Xin chào, " + u.getDisplayName() + " (" + u.getRole().getLabel() + ")";
        JLabel lbl = new JLabel(who);
        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.addActionListener(e -> onLogout.run());

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(BorderFactory.createEmptyBorder(6, 12, 2, 12));
        bar.add(lbl, BorderLayout.WEST);
        bar.add(btnLogout, BorderLayout.EAST);
        return bar;
    }
}
