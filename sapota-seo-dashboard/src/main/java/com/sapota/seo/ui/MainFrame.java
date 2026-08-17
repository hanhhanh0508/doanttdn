package com.sapota.seo.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Cửa sổ chính của ứng dụng "Dashboard Backlink Gap & Dofollow Checker"
 * (tương ứng khung giao diện tại Hình 4.4 / 4.5 trong báo cáo thực tập).
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        super("SapotaCorp - Dashboard Backlink Gap & Dofollow Checker");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 500));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Dashboard", new DashboardPanel());
        tabbedPane.addTab("Dofollow Checker", new CheckerPanel());
        tabbedPane.addTab("Tạo bài Post (AI)", new PostGeneratorPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }
}
