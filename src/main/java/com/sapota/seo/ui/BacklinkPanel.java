package com.sapota.seo.ui;

import com.sapota.seo.dao.BacklinkDAO;
import com.sapota.seo.model.BacklinkDomain;
import com.sapota.seo.service.ExcelImportService;
import com.sapota.seo.util.CsvExporter;
import com.sapota.seo.util.ExcelExporter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

/** Tab "Backlink hiện có": xem danh sách domain đang trỏ về sapotacorp.vn, tìm kiếm, lọc, nhập/xuất. */
public class BacklinkPanel extends JPanel implements Refreshable {

    private final BacklinkDAO dao = new BacklinkDAO();
    private final DefaultTableModel model = UiUtil.newModel(
            new String[]{"Domain", "AS", "Backlinks", "Quốc gia"},
            new Class<?>[]{String.class, Integer.class, Integer.class, String.class});
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    private final JTextField txtSearch = new JTextField(14);
    private final JTextField txtMinAs = new JTextField("0", 4);
    private final JLabel lblSummary = new JLabel(" ");

    public BacklinkPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        bar.add(new JLabel("Tìm domain:"));
        bar.add(txtSearch);
        bar.add(new JLabel("AS tối thiểu:"));
        bar.add(txtMinAs);
        JButton btnFilter = new JButton("Áp dụng lọc");
        btnFilter.addActionListener(e -> applyFilter());
        txtSearch.addActionListener(e -> applyFilter());
        txtMinAs.addActionListener(e -> applyFilter());
        JButton btnImport = new JButton("Nhập từ Excel...");
        btnImport.addActionListener(e -> importExcel());
        bar.add(btnFilter);
        bar.add(btnImport);
        add(bar, BorderLayout.NORTH);

        table.setRowHeight(24);
        table.setRowSorter(sorter);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    UiUtil.openBrowser(BacklinkPanel.this, (String) table.getValueAt(table.getSelectedRow(), 0));
                }
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(lblSummary, BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCsv = new JButton("Xuất CSV");
        btnCsv.addActionListener(e -> export(false));
        JButton btnXlsx = new JButton("Xuất Excel");
        btnXlsx.addActionListener(e -> export(true));
        right.add(new JLabel("(nhấp đúp để mở website)"));
        right.add(btnCsv);
        right.add(btnXlsx);
        bottom.add(right, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        refresh();
    }

    @Override
    public void refresh() {
        List<BacklinkDomain> list = dao.findAll();
        model.setRowCount(0);
        for (BacklinkDomain b : list) {
            model.addRow(new Object[]{b.getDomain(), b.getAscore(), b.getBacklinks(), b.getCountry()});
        }
        applyFilter();
    }

    private void applyFilter() {
        final String kw = txtSearch.getText().trim().toLowerCase();
        final int minAs = UiUtil.parseIntSafe(txtMinAs.getText(), 0);
        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> en) {
                String domain = String.valueOf(en.getValue(0)).toLowerCase();
                int as = (Integer) en.getValue(1);
                return domain.contains(kw) && as >= minAs;
            }
        });
        long totalBl = 0;
        double sumAs = 0;
        for (int r = 0; r < table.getRowCount(); r++) {
            sumAs += (Integer) table.getValueAt(r, 1);
            totalBl += (Integer) table.getValueAt(r, 2);
        }
        int n = table.getRowCount();
        lblSummary.setText(String.format("Hiển thị %d/%d domain | Tổng backlink: %d | AS trung bình: %.1f",
                n, model.getRowCount(), totalBl, n == 0 ? 0 : sumAs / n));
    }

    private void importExcel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn file Excel Backlink hiện có (sapotacorp_vn-backlinks_refdomains.xlsx)");
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile();
        if (!UiUtil.confirm(this, "Dữ liệu Backlink hiện có sẽ được thay thế bằng file này. Tiếp tục?")) return;
        try {
            int n = new ExcelImportService().importBacklinks(f.getAbsolutePath());
            refresh();
            UiUtil.info(this, "Đã nhập " + n + " domain.");
        } catch (Exception ex) {
            UiUtil.error(this, "Lỗi khi đọc file Excel: " + ex.getMessage());
        }
    }

    private void export(boolean excel) {
        String path = UiUtil.chooseSavePath(this, "Lưu danh sách backlink hiện có",
                excel ? "backlink_hien_co.xlsx" : "backlink_hien_co.csv");
        if (path == null) return;
        try {
            if (excel) ExcelExporter.exportTable(table, "Backlink hiện có", path);
            else CsvExporter.export(table, path);
            UiUtil.info(this, "Đã xuất file thành công.");
        } catch (Exception ex) {
            UiUtil.error(this, "Lỗi khi xuất file: " + ex.getMessage());
        }
    }
}
