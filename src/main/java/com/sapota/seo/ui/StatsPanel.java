package com.sapota.seo.ui;

import com.sapota.seo.service.ReportService;
import com.sapota.seo.service.StatsService;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/** Tab Thống kê: các thẻ chỉ số tổng quan + 4 biểu đồ + xuất báo cáo Excel tổng hợp. */
public class StatsPanel extends JPanel implements Refreshable {

    private final StatsService statsService = new StatsService();

    private final JPanel cards = new JPanel(new GridLayout(2, 5, 8, 8));
    private final SimpleChartPanel pieCheck = new SimpleChartPanel("Kết quả kiểm tra Dofollow", SimpleChartPanel.Type.PIE);
    private final SimpleChartPanel barCompetitors = new SimpleChartPanel("Đối thủ xuất hiện nhiều nhất trong Gap", SimpleChartPanel.Type.BAR_HORIZONTAL);
    private final SimpleChartPanel barAs = new SimpleChartPanel("Phân bố AS của domain Gap", SimpleChartPanel.Type.BAR_VERTICAL);
    private final SimpleChartPanel barOutreach = new SimpleChartPanel("Tiến độ Outreach theo trạng thái", SimpleChartPanel.Type.BAR_VERTICAL);

    public StatsPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(cards, BorderLayout.NORTH);

        JPanel charts = new JPanel(new GridLayout(2, 2, 8, 8));
        charts.add(barAs);
        charts.add(barCompetitors);
        charts.add(pieCheck);
        charts.add(barOutreach);
        add(charts, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.addActionListener(e -> refresh());
        JButton btnReport = new JButton("Xuất báo cáo tổng hợp (Excel)");
        btnReport.addActionListener(e -> exportReport());
        bottom.add(btnRefresh);
        bottom.add(btnReport);
        add(bottom, BorderLayout.SOUTH);

        refresh();
    }

    @Override
    public void refresh() {
        StatsService.Snapshot s = statsService.compute();

        cards.removeAll();
        addCard("Domain Backlink Gap", String.valueOf(s.gapCount), new Color(0x2E86DE));
        addCard("AS tối đa trung bình (Gap)", String.format("%.1f", s.gapAvgAs), new Color(0x2E86DE));
        addCard("Domain ưu tiên cao (≥ 80)", String.valueOf(s.highPriorityCount), new Color(0xEE5253));
        addCard("Domain đang trỏ về (hiện có)", String.valueOf(s.refDomainCount), new Color(0x10AC84));
        addCard("Tổng backlink hiện có", String.valueOf(s.totalBacklinks), new Color(0x10AC84));
        addCard("Tỷ lệ Dofollow đã kiểm tra", String.format("%.1f%%", s.dofollowRate), new Color(0x5F27CD));
        addCard("Domain đang Outreach", String.valueOf(s.outreachTotal), new Color(0xFF9F43));
        addCard("Outreach thành công", s.outreachWon + " (" + String.format("%.0f%%", s.outreachWinRate) + ")", new Color(0x10AC84));
        addCard("Follow-up quá hạn", String.valueOf(s.overdueFollowUps), new Color(0xEE5253));
        addCard("Backlink bị mất/đổi", s.trackedLost + " / " + s.trackedTotal, new Color(0xEE5253));
        cards.revalidate();
        cards.repaint();

        pieCheck.setData(s.checkByStatus);
        barCompetitors.setData(s.topCompetitors);
        barAs.setData(s.asDistribution);
        barOutreach.setData(s.outreachByStatus);
    }

    private void addCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, color),
                BorderFactory.createEmptyBorder(6, 10, 6, 6)));
        card.setBackground(Color.WHITE);
        JLabel v = new JLabel(value);
        v.setFont(v.getFont().deriveFont(Font.BOLD, 20f));
        v.setForeground(color);
        JLabel t = new JLabel(title);
        t.setFont(t.getFont().deriveFont(11f));
        t.setForeground(Color.GRAY);
        card.add(v, BorderLayout.CENTER);
        card.add(t, BorderLayout.SOUTH);
        cards.add(card);
    }

    private void exportReport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu báo cáo tổng hợp");
        chooser.setSelectedFile(new File("bao_cao_tong_hop_seo.xlsx"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            new ReportService().exportFullReport(chooser.getSelectedFile().getAbsolutePath());
            UiUtil.info(this, "Đã xuất báo cáo tổng hợp (6 sheet) thành công.");
        } catch (Exception ex) {
            UiUtil.error(this, "Lỗi khi xuất báo cáo: " + ex.getMessage());
        }
    }
}
