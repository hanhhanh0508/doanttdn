package com.sapota.seo.ui;

import com.sapota.seo.dao.GapDomainDAO;
import com.sapota.seo.model.GapDomain;
import com.sapota.seo.service.ExcelImportService;
import com.sapota.seo.util.CsvExporter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Màn hình Dashboard Backlink Gap (tương ứng wireframe Hình 4.4 trong báo cáo).
 * Hiển thị bảng xếp hạng domain ưu tiên, cho phép lọc theo AS / số đối thủ,
 * và nhập dữ liệu mới từ file Excel.
 */
public class DashboardPanel extends JPanel {

    private final GapDomainDAO gapDomainDAO = new GapDomainDAO();

    private JTextField txtMinAs;
    private JTextField txtMinCompetitor;
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel lblSummary;
    private PriorityChartPanel chartPanel;

    public DashboardPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildFilterBar(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        loadData();
    }

    /**
     * Khu vực trung tâm: bảng dữ liệu bên trái + biểu đồ trực quan bên phải
     * (khu vực biểu đồ tương ứng Hình 4.4 trong wireframe báo cáo, mục 4.2.1).
     */
    private JSplitPane buildCenter() {
        chartPanel = new PriorityChartPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildTable(), chartPanel);
        splitPane.setResizeWeight(0.62); // ưu tiên không gian cho bảng
        splitPane.setBorder(null);
        splitPane.setDividerSize(6);
        return splitPane;
    }

    private JPanel buildFilterBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        panel.add(new JLabel("AS tối thiểu:"));
        txtMinAs = new JTextField("0", 4);
        panel.add(txtMinAs);

        panel.add(new JLabel("Số đối thủ tối thiểu:"));
        txtMinCompetitor = new JTextField("0", 4);
        panel.add(txtMinCompetitor);

        JButton btnFilter = new JButton("Áp dụng lọc");
        btnFilter.addActionListener(e -> loadData());
        panel.add(btnFilter);

        JButton btnImport = new JButton("Nhập dữ liệu từ Excel...");
        btnImport.addActionListener(e -> importExcel());
        panel.add(btnImport);

        return panel;
    }

    private JScrollPane buildTable() {
        String[] columns = {"Domain", "MaxAS", "Số đối thủ", "Điểm ưu tiên", "Đối thủ sở hữu"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.getColumnModel().getColumn(4).setPreferredWidth(320);
        return new JScrollPane(table);
    }

    private JPanel buildBottomBar() {
        JPanel panel = new JPanel(new BorderLayout());
        lblSummary = new JLabel(" ");
        panel.add(lblSummary, BorderLayout.WEST);

        JButton btnExport = new JButton("Xuất báo cáo (CSV)");
        btnExport.addActionListener(e -> exportCsv());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.add(btnExport);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    private void loadData() {
        int minAs = parseIntSafe(txtMinAs.getText(), 0);
        int minCompetitor = parseIntSafe(txtMinCompetitor.getText(), 0);

        List<GapDomain> list = gapDomainDAO.findAll(minAs, minCompetitor);
        tableModel.setRowCount(0);
        for (GapDomain g : list) {
            tableModel.addRow(new Object[]{
                    g.getDomain(), g.getMaxAs(), g.getCompetitorCount(),
                    String.format("%.1f", g.getPriorityScore()), g.getCompetitors()
            });
        }
        lblSummary.setText("Tổng số domain: " + list.size());
        chartPanel.setData(list);
    }

    private void importExcel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn file Excel Backlink Gap (sapota-gap-refdomains-as30-dofollow.xlsx)");
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        try {
            ExcelImportService importService = new ExcelImportService();
            int count = importService.importGapDomains(file.getAbsolutePath());
            JOptionPane.showMessageDialog(this, "Đã nhập " + count + " dòng dữ liệu thành công.",
                    "Nhập dữ liệu", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi đọc file Excel: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu báo cáo Backlink Gap");
        chooser.setSelectedFile(new File("backlink_gap_report.csv"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        try {
            CsvExporter.export(table, chooser.getSelectedFile().getAbsolutePath());
            JOptionPane.showMessageDialog(this, "Đã xuất báo cáo thành công.",
                    "Xuất báo cáo", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xuất file: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int parseIntSafe(String s, int defaultValue) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }
}