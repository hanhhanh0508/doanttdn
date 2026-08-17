package com.sapota.seo.ui;

import com.sapota.seo.model.GapDomain;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Biểu đồ cột ngang (horizontal bar chart) trực quan hoá Top N domain có
 * điểm ưu tiên cao nhất trong bảng gap_domains.
 * <p>
 * Đây là phần "biểu đồ trực quan" của màn hình Dashboard, tương ứng yêu cầu
 * chức năng tại mục 4.2.1 và khu vực biểu đồ trong wireframe (Hình 4.4)
 * của báo cáo thực tập — trước đây khu vực này chỉ để trống.
 * <p>
 * Được vẽ trực tiếp bằng Java2D (không phụ thuộc thư viện biểu đồ ngoài
 * như JFreeChart) để tránh phát sinh dependency Maven mới.
 */
public class PriorityChartPanel extends JPanel {

    private static final int MAX_BARS = 10;
    private static final Color BAR_COLOR_HIGH = new Color(0x1B, 0x74, 0xE4);
    private static final Color BAR_COLOR_LOW = new Color(0x8E, 0xC5, 0xFC);
    private static final Color GRID_COLOR = new Color(0xE3, 0xE6, 0xEA);
    private static final Color TEXT_COLOR = new Color(0x33, 0x33, 0x33);
    private static final DecimalFormat SCORE_FORMAT = new DecimalFormat("#0.0");

    private List<GapDomain> data = new ArrayList<>();
    private String title = "Top domain ưu tiên theo điểm số";

    public PriorityChartPanel() {
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD)),
                BorderFactory.createEmptyBorder(12, 16, 12, 20)));
        setPreferredSize(new Dimension(420, 400));
    }

    /** Cập nhật dữ liệu và vẽ lại biểu đồ. Tự lấy Top {@value #MAX_BARS} theo điểm ưu tiên. */
    public void setData(List<GapDomain> newData) {
        List<GapDomain> sorted = new ArrayList<>(newData);
        sorted.sort(Comparator.comparingDouble(GapDomain::getPriorityScore).reversed());
        this.data = sorted.size() > MAX_BARS ? sorted.subList(0, MAX_BARS) : sorted;
        this.title = "Top " + this.data.size() + " domain ưu tiên theo điểm số";
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        Insets insets = getInsets();
        int contentX = insets.left;
        int contentY = insets.top;
        int contentWidth = width - insets.left - insets.right;
        int contentHeight = height - insets.top - insets.bottom;

        // Tiêu đề
        g2.setFont(getFont().deriveFont(Font.BOLD, 14f));
        g2.setColor(TEXT_COLOR);
        g2.drawString(title, contentX, contentY + 16);

        if (data.isEmpty()) {
            g2.setFont(getFont().deriveFont(Font.PLAIN, 13f));
            g2.setColor(Color.GRAY);
            g2.drawString("Chưa có dữ liệu để hiển thị. Hãy nhập dữ liệu từ Excel.",
                    contentX, contentY + 50);
            g2.dispose();
            return;
        }

        int chartTop = contentY + 34;
        int chartBottom = contentY + contentHeight - 10;
        int labelWidth = 130; // vùng tên domain bên trái
        int scoreLabelWidth = 55; // vùng số điểm bên phải thanh
        int chartLeft = contentX + labelWidth;
        int chartRight = contentX + contentWidth - scoreLabelWidth;
        int chartAreaWidth = Math.max(10, chartRight - chartLeft);

        int barCount = data.size();
        int gap = 8;
        int barHeight = Math.max(12, (chartBottom - chartTop - gap * (barCount - 1)) / barCount);
        barHeight = Math.min(barHeight, 28);

        double maxScore = data.stream().mapToDouble(GapDomain::getPriorityScore).max().orElse(1.0);
        if (maxScore <= 0) maxScore = 1.0;

        // Lưới dọc (gridlines) tham chiếu điểm số
        g2.setColor(GRID_COLOR);
        g2.setFont(getFont().deriveFont(Font.PLAIN, 10f));
        int gridLines = 4;
        for (int i = 0; i <= gridLines; i++) {
            int x = chartLeft + (int) ((double) i / gridLines * chartAreaWidth);
            g2.setColor(GRID_COLOR);
            g2.drawLine(x, chartTop - 4, x, chartTop + barCount * (barHeight + gap));
            double value = maxScore * i / gridLines;
            String label = SCORE_FORMAT.format(value);
            g2.setColor(Color.GRAY);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label, x - fm.stringWidth(label) / 2, chartTop - 8);
        }

        int y = chartTop;
        g2.setFont(getFont().deriveFont(Font.PLAIN, 12f));
        FontMetrics labelFm = g2.getFontMetrics();

        for (GapDomain gd : data) {
            double ratio = gd.getPriorityScore() / maxScore;
            int barWidth = (int) Math.round(ratio * chartAreaWidth);
            barWidth = Math.max(barWidth, 2);

            // Tên domain (rút gọn nếu quá dài)
            String domainLabel = truncateToFit(gd.getDomain(), labelFm, labelWidth - 8);
            g2.setColor(TEXT_COLOR);
            int textY = y + barHeight / 2 + labelFm.getAscent() / 2 - 2;
            int textX = chartLeft - 8 - labelFm.stringWidth(domainLabel);
            g2.drawString(domainLabel, Math.max(contentX, textX), textY);

            // Thanh cột (gradient theo tỉ lệ điểm)
            Color barColor = blend(BAR_COLOR_LOW, BAR_COLOR_HIGH, ratio);
            GradientPaint gradient = new GradientPaint(
                    chartLeft, y, barColor.brighter(),
                    chartLeft + barWidth, y, barColor);
            g2.setPaint(gradient);
            RoundRectangle2D bar = new RoundRectangle2D.Float(
                    chartLeft, y, barWidth, barHeight, 6, 6);
            g2.fill(bar);

            // Nhãn điểm số cuối thanh
            g2.setColor(TEXT_COLOR);
            g2.setFont(getFont().deriveFont(Font.BOLD, 11f));
            String scoreLabel = SCORE_FORMAT.format(gd.getPriorityScore());
            g2.drawString(scoreLabel, chartLeft + barWidth + 6, y + barHeight / 2 + 4);
            g2.setFont(getFont().deriveFont(Font.PLAIN, 12f));

            y += barHeight + gap;
        }

        g2.dispose();
    }

    private String truncateToFit(String text, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth) return text;
        String ellipsis = "...";
        StringBuilder sb = new StringBuilder(text);
        while (sb.length() > 0 && fm.stringWidth(sb.toString() + ellipsis) > maxWidth) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb + ellipsis;
    }

    private Color blend(Color c1, Color c2, double ratio) {
        ratio = Math.max(0, Math.min(1, ratio));
        int r = (int) (c1.getRed() + ratio * (c2.getRed() - c1.getRed()));
        int g = (int) (c1.getGreen() + ratio * (c2.getGreen() - c1.getGreen()));
        int b = (int) (c1.getBlue() + ratio * (c2.getBlue() - c1.getBlue()));
        return new Color(r, g, b);
    }

    @Override
    public Dimension getPreferredSize() {
        int rows = Math.max(data.size(), 1);
        int height = 60 + rows * 36;
        return new Dimension(420, Math.max(height, 200));
    }
}