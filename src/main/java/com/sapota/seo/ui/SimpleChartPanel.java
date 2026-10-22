package com.sapota.seo.ui;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/** Biểu đồ đơn giản vẽ bằng Java2D: cột đứng, cột ngang hoặc tròn (pie). */
public class SimpleChartPanel extends JPanel {

    public enum Type { BAR_VERTICAL, BAR_HORIZONTAL, PIE }

    private static final Color[] PALETTE = {
            new Color(0x2E86DE), new Color(0x10AC84), new Color(0xFF9F43), new Color(0xEE5253),
            new Color(0x8395A7), new Color(0x5F27CD), new Color(0x01A3A4), new Color(0xF368E0)
    };

    private final Type type;
    private Map<String, ? extends Number> data = new LinkedHashMap<String, Integer>();

    public SimpleChartPanel(String title, Type type) {
        this.type = type;
        setBorder(BorderFactory.createTitledBorder(title));
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(380, 230));
    }

    public void setData(Map<String, ? extends Number> data) {
        this.data = data;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Insets in = getInsets();
        Rectangle area = new Rectangle(in.left + 8, in.top + 4, getWidth() - in.left - in.right - 16,
                getHeight() - in.top - in.bottom - 8);

        double total = 0;
        double max = 0;
        for (Number n : data.values()) {
            total += n.doubleValue();
            max = Math.max(max, n.doubleValue());
        }
        if (data.isEmpty() || total == 0) {
            g.setColor(Color.GRAY);
            String msg = "Chưa có dữ liệu";
            g.drawString(msg, area.x + (area.width - g.getFontMetrics().stringWidth(msg)) / 2, area.y + area.height / 2);
            g.dispose();
            return;
        }

        switch (type) {
            case PIE: drawPie(g, area, total); break;
            case BAR_HORIZONTAL: drawHBars(g, area, max); break;
            default: drawVBars(g, area, max); break;
        }
        g.dispose();
    }

    private void drawPie(Graphics2D g, Rectangle a, double total) {
        int size = Math.min(a.height - 4, a.width / 2);
        int cx = a.x + 4, cy = a.y + (a.height - size) / 2;
        double start = 90;
        int i = 0;
        for (Map.Entry<String, ? extends Number> e : data.entrySet()) {
            double v = e.getValue().doubleValue();
            if (v > 0) {
                double angle = v / total * 360.0;
                g.setColor(PALETTE[i % PALETTE.length]);
                g.fillArc(cx, cy, size, size, (int) Math.round(start), (int) -Math.round(angle));
                start -= angle;
            }
            i++;
        }
        // chú thích bên phải
        int lx = cx + size + 16, ly = a.y + 20;
        i = 0;
        FontMetrics fm = g.getFontMetrics();
        for (Map.Entry<String, ? extends Number> e : data.entrySet()) {
            g.setColor(PALETTE[i % PALETTE.length]);
            g.fillRect(lx, ly - 10, 12, 12);
            g.setColor(Color.DARK_GRAY);
            double v = e.getValue().doubleValue();
            g.drawString(e.getKey() + ": " + (int) v + " (" + Math.round(v / total * 100) + "%)", lx + 18, ly);
            ly += fm.getHeight() + 6;
            i++;
        }
    }

    private void drawVBars(Graphics2D g, Rectangle a, double max) {
        int n = data.size();
        int labelH = 18, valueH = 14;
        int chartH = a.height - labelH - valueH;
        int slot = a.width / n;
        int barW = Math.max(10, slot - 14);
        int i = 0;
        FontMetrics fm = g.getFontMetrics();
        for (Map.Entry<String, ? extends Number> e : data.entrySet()) {
            double v = e.getValue().doubleValue();
            int h = (int) Math.round(v / max * chartH);
            int x = a.x + i * slot + (slot - barW) / 2;
            int y = a.y + valueH + chartH - h;
            g.setColor(PALETTE[i % PALETTE.length]);
            g.fillRoundRect(x, y, barW, Math.max(h, 1), 6, 6);
            g.setColor(Color.DARK_GRAY);
            String val = String.valueOf((int) v);
            g.drawString(val, x + (barW - fm.stringWidth(val)) / 2, y - 3);
            String label = shorten(e.getKey(), fm, slot);
            g.drawString(label, a.x + i * slot + (slot - fm.stringWidth(label)) / 2, a.y + a.height - 4);
            i++;
        }
    }

    private void drawHBars(Graphics2D g, Rectangle a, double max) {
        int n = data.size();
        FontMetrics fm = g.getFontMetrics();
        int labelW = 0;
        for (String k : data.keySet()) labelW = Math.max(labelW, fm.stringWidth(k));
        labelW = Math.min(labelW, a.width / 2) + 8;
        int rowH = Math.max(16, a.height / n);
        int barH = Math.max(8, rowH - 8);
        int maxW = a.width - labelW - 40;
        int i = 0;
        for (Map.Entry<String, ? extends Number> e : data.entrySet()) {
            double v = e.getValue().doubleValue();
            int y = a.y + i * rowH + (rowH - barH) / 2;
            int w = (int) Math.round(v / max * maxW);
            g.setColor(Color.DARK_GRAY);
            g.drawString(shorten(e.getKey(), fm, labelW), a.x, y + barH - 2);
            g.setColor(PALETTE[i % PALETTE.length]);
            g.fillRoundRect(a.x + labelW, y, Math.max(w, 2), barH, 6, 6);
            g.setColor(Color.DARK_GRAY);
            g.drawString(String.valueOf((int) v), a.x + labelW + w + 5, y + barH - 2);
            i++;
        }
    }

    private String shorten(String s, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(s) <= maxWidth) return s;
        while (s.length() > 1 && fm.stringWidth(s + "…") > maxWidth) s = s.substring(0, s.length() - 1);
        return s + "…";
    }
}
