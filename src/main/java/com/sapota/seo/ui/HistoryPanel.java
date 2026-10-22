package com.sapota.seo.ui;

import com.sapota.seo.dao.CheckResultDAO;
import com.sapota.seo.model.CheckResult;
import com.sapota.seo.util.CsvExporter;
import com.sapota.seo.util.ExcelExporter;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Tab "Lịch sử kiểm tra": xem lại toàn bộ kết quả Dofollow đã lưu trong MySQL, lọc, xoá, kiểm tra lại. */
public class HistoryPanel extends JPanel implements Refreshable {

    private final CheckResultDAO dao = new CheckResultDAO();
    private final DefaultTableModel model = UiUtil.newModel(
            new String[]{"URL", "Trạng thái", "Thời gian kiểm tra", "Ghi chú"}, null);
    private final JTable table = new JTable(model);
    private final JComboBox<String> cbStatus = new JComboBox<>(new String[]{
            "Tất cả", CheckResult.STATUS_DOFOLLOW, CheckResult.STATUS_NOFOLLOW,
            CheckResult.STATUS_NOT_FOUND, CheckResult.STATUS_ERROR});
    private final JTextField txtSearch = new JTextField(18);
    private final JLabel lblSummary = new JLabel(" ");
    private final JProgressBar progress = new JProgressBar();
    private List<CheckResult> current = new ArrayList<>();

    public HistoryPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        bar.add(new JLabel("Trạng thái:"));
        bar.add(cbStatus);
        bar.add(new JLabel("URL chứa:"));
        bar.add(txtSearch);
        JButton btnFilter = new JButton("Lọc / Làm mới");
        btnFilter.addActionListener(e -> refresh());
        txtSearch.addActionListener(e -> refresh());
        bar.add(btnFilter);
        add(bar, BorderLayout.NORTH);

        table.setRowHeight(24);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(320);
        table.getColumnModel().getColumn(3).setPreferredWidth(300);
        table.getColumnModel().getColumn(1).setCellRenderer(new StatusRenderer());
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.add(lblSummary);
        progress.setStringPainted(true);
        progress.setVisible(false);
        left.add(progress);
        bottom.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRecheck = new JButton("Kiểm tra lại mục đã chọn");
        btnRecheck.addActionListener(e -> recheckSelected());
        JButton btnDelete = new JButton("Xoá mục đã chọn");
        btnDelete.addActionListener(e -> deleteSelected());
        JButton btnClear = new JButton("Xoá toàn bộ lịch sử");
        btnClear.addActionListener(e -> clearAll());
        JButton btnCsv = new JButton("Xuất CSV");
        btnCsv.addActionListener(e -> export(false));
        JButton btnXlsx = new JButton("Xuất Excel");
        btnXlsx.addActionListener(e -> export(true));
        right.add(btnRecheck);
        right.add(btnDelete);
        right.add(btnClear);
        right.add(btnCsv);
        right.add(btnXlsx);
        bottom.add(right, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        refresh();
    }

    @Override
    public void refresh() {
        String status = "Tất cả".equals(cbStatus.getSelectedItem()) ? null : (String) cbStatus.getSelectedItem();
        current = dao.findFiltered(status, txtSearch.getText().trim(), 2000);
        model.setRowCount(0);
        for (CheckResult r : current) {
            model.addRow(new Object[]{r.getUrl(), r.getStatus(), r.getCheckedAtFormatted(), r.getNote()});
        }
        Map<String, Integer> counts = dao.countByStatus();
        lblSummary.setText("Hiển thị " + current.size() + " dòng | Toàn bộ: Dofollow "
                + counts.get(CheckResult.STATUS_DOFOLLOW) + ", Nofollow " + counts.get(CheckResult.STATUS_NOFOLLOW)
                + ", Không tìm thấy " + counts.get(CheckResult.STATUS_NOT_FOUND)
                + ", Lỗi " + counts.get(CheckResult.STATUS_ERROR));
    }

    private List<CheckResult> selected() {
        List<CheckResult> sel = new ArrayList<>();
        for (int v : table.getSelectedRows()) sel.add(current.get(table.convertRowIndexToModel(v)));
        return sel;
    }

    private void deleteSelected() {
        List<CheckResult> sel = selected();
        if (sel.isEmpty()) {
            UiUtil.warn(this, "Hãy chọn ít nhất 1 dòng.");
            return;
        }
        if (!UiUtil.confirm(this, "Xoá " + sel.size() + " dòng đã chọn?")) return;
        for (CheckResult r : sel) dao.deleteById(r.getId());
        refresh();
    }

    private void clearAll() {
        if (!UiUtil.confirm(this, "Xoá TOÀN BỘ lịch sử kiểm tra? Thao tác này không hoàn tác được.")) return;
        dao.deleteAll();
        refresh();
    }

    /** Kiểm tra lại các URL đã chọn với domain đích mặc định; kết quả mới được lưu thành dòng mới trong lịch sử. */
    private void recheckSelected() {
        List<CheckResult> sel = selected();
        if (sel.isEmpty()) {
            UiUtil.warn(this, "Hãy chọn ít nhất 1 dòng để kiểm tra lại.");
            return;
        }
        List<String> urls = new ArrayList<>();
        for (CheckResult r : sel) if (!urls.contains(r.getUrl())) urls.add(r.getUrl());
        String target = com.sapota.seo.util.AppSettings.getTargetDomain();

        progress.setMaximum(urls.size());
        progress.setValue(0);
        progress.setVisible(true);
        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                com.sapota.seo.service.BatchCheckService.checkAll(urls, target, 5, (i, res, done, total) -> {
                    dao.insert(res);
                    publish(done);
                });
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                progress.setValue(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                progress.setVisible(false);
                refresh();
            }
        };
        worker.execute();
    }

    private void export(boolean excel) {
        if (table.getRowCount() == 0) {
            UiUtil.info(this, "Chưa có dữ liệu để xuất.");
            return;
        }
        String path = UiUtil.chooseSavePath(this, "Lưu lịch sử kiểm tra",
                excel ? "lich_su_kiem_tra.xlsx" : "lich_su_kiem_tra.csv");
        if (path == null) return;
        try {
            if (excel) ExcelExporter.exportTable(table, "Lịch sử kiểm tra", path);
            else CsvExporter.export(table, path);
            UiUtil.info(this, "Đã xuất file thành công.");
        } catch (Exception ex) {
            UiUtil.error(this, "Lỗi khi xuất file: " + ex.getMessage());
        }
    }

    /** Tô màu ô trạng thái: xanh = Dofollow, cam = Nofollow, xám = không thấy, đỏ = lỗi. */
    static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
            Component comp = super.getTableCellRendererComponent(t, v, sel, focus, r, c);
            if (!sel) {
                String s = String.valueOf(v);
                Color bg = Color.WHITE;
                if (CheckResult.STATUS_DOFOLLOW.equals(s)) bg = new Color(0xD5F5E3);
                else if (CheckResult.STATUS_NOFOLLOW.equals(s)) bg = new Color(0xFDEBD0);
                else if (CheckResult.STATUS_ERROR.equals(s)) bg = new Color(0xFADBD8);
                else if (CheckResult.STATUS_NOT_FOUND.equals(s)) bg = new Color(0xEAECEE);
                comp.setBackground(bg);
                comp.setForeground(Color.BLACK);
            }
            return comp;
        }
    }
}
