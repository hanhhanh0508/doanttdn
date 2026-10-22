package com.sapota.seo.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Cửa sổ chính của ứng dụng SapotaCorp SEO Dashboard.
 * Gồm 9 tab; tab nào cần dữ liệu mới (Refreshable) sẽ tự nạp lại khi được chọn.
 */
public class MainFrame extends JFrame {

    public MainFrame() {
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
        tabbedPane.addTab("Cài đặt", new SettingsPanel());

        // Chuyển tab thì nạp lại dữ liệu để các tab luôn đồng bộ với nhau
        tabbedPane.addChangeListener(e -> {
            Component c = tabbedPane.getSelectedComponent();
            if (c instanceof Refreshable) ((Refreshable) c).refresh();
        });

        add(tabbedPane, BorderLayout.CENTER);
    }
}
