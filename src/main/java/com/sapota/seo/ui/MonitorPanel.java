package com.sapota.seo.ui;

import com.sapota.seo.dao.CheckResultDAO;
import com.sapota.seo.dao.TrackedLinkDAO;
import com.sapota.seo.model.CheckResult;
import com.sapota.seo.model.TrackedLink;
import com.sapota.seo.service.BatchCheckService;
import com.sapota.seo.util.AppSettings;
import com.sapota.seo.util.ExcelExporter;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tab Backlink Monitor: danh sách backlink đã đặt được, kiểm tra lại định kỳ để phát hiện
 * link bị gỡ hoặc bị đổi từ dofollow sang nofollow (cảnh báo mất link).
 */
public class MonitorPanel extends JPanel implements Refreshable {

    private final TrackedLinkDAO dao = new TrackedLinkDAO();
    private final CheckResultDAO historyDao = new CheckResultDAO();
    private final DefaultTableModel model = UiUtil.newModel(
            new String[]{"URL", "Nền tảng", "Ngày đặt", "Trạng thái", "Trạng thái trước", "Kiểm tra lần cuối", "Cảnh báo", "Ghi chú"}, null);
    private final JTable table = new JTable(model);
    private final JLabel lblSummary = new JLabel(" ");
    private final JProgressBar progress = new JProgressBar();
    private final JButton btnCheckAll = new JButton("Kiểm tra tất cả");
    private final JButton btnCheckSel = new JButton("Kiểm tra mục đã chọn");
    private List<TrackedLink> current = new ArrayList<>();

    public MonitorPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel hint = new JLabel("Theo dõi các backlink đã đặt: app kiểm tra lại và cảnh báo nếu link bị gỡ hoặc chuyển từ Dofollow sang trạng thái khác.");
        hint.setForeground(Color.GRAY);
        add(hint, BorderLayout.NORTH);

        table.setRowHeight(24);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(300);
        table.getColumnModel().getColumn(7).setPreferredWidth(200);
        table.setDefaultRenderer(Object.class, new RowRenderer());
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.add(lblSummary);
        progress.setStringPainted(true);
        progress.setVisible(false);
        left.add(progress);
        bottom.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.add(button("Thêm link", e -> addOne()));
        right.add(button("Thêm nhiều URL", e -> addMany()));
        btnCheckSel.addActionListener(e -> runCheck(false));
        btnCheckAll.addActionListener(e -> runCheck(true));
        right.add(btnCheckSel);
        right.add(btnCheckAll);
        right.add(button("Mở link", e -> openLink()));
        right.add(button("Xoá", e -> deleteSelected()));
        right.add(button("Xuất Excel", e -> exportExcel()));
        bottom.add(right, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        refresh();
    }

    private JButton button(String text, java.awt.event.ActionListener l) {
        JButton b = new JButton(text);
        b.addActionListener(l);
        return b;
    }

    @Override
    public void refresh() {
        current = dao.findAll();
        model.setRowCount(0);
        int ok = 0, lost = 0;
        for (TrackedLink t : current) {
            if (CheckResult.STATUS_DOFOLLOW.equals(t.getLastStatus())) ok++;
            if (t.isLost()) lost++;
            model.addRow(new Object[]{t.getUrl(), t.getPlatform(), UiUtil.formatDate(t.getPlacedDate()),
                    t.getLastStatus() == null ? "Chưa kiểm tra" : t.getLastStatus(),
                    t.getPrevStatus() == null ? "" : t.getPrevStatus(),
                    t.getLastChecked() == null ? "" : t.getLastChecked().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    t.isLost() ? "MẤT/ĐỔI LINK" : "", t.getNote()});
        }
        lblSummary.setText("Tổng: " + current.size() + " | Đang Dofollow: " + ok + " | Cảnh báo mất link: " + lost);
    }

    private void addOne() {
        JTextField fUrl = new JTextField(30);
        JTextField fPlatform = new JTextField(20);
        JTextField fDate = new JTextField(UiUtil.formatDate(LocalDate.now()), 20);
        JTextField fNote = new JTextField(20);
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("URL trang chứa link:")); form.add(fUrl);
        form.add(new JLabel("Nền tảng/website:")); form.add(fPlatform);
        form.add(new JLabel("Ngày đặt (dd/MM/yyyy):")); form.add(fDate);
        form.add(new JLabel("Ghi chú:")); form.add(fNote);
        if (JOptionPane.showConfirmDialog(this, form, "Thêm backlink theo dõi",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;

        String url = fUrl.getText().trim();
        if (url.isEmpty()) {
            UiUtil.warn(this, "URL không được để trống.");
            return;
        }
        try {
            TrackedLink t = new TrackedLink();
            t.setUrl(url);
            t.setPlatform(fPlatform.getText().trim());
            t.setPlacedDate(UiUtil.parseDate(fDate.getText()));
            t.setNote(fNote.getText().trim());
            if (!dao.insert(t)) UiUtil.warn(this, "URL này đã có trong danh sách theo dõi (hoặc có lỗi lưu).");
            refresh();
        } catch (DateTimeParseException ex) {
            UiUtil.warn(this, "Ngày không hợp lệ. Hãy nhập dạng dd/MM/yyyy.");
        }
    }

    private void addMany() {
        JTextArea area = new JTextArea(10, 50);
        if (JOptionPane.showConfirmDialog(this, new JScrollPane(area),
                "Dán danh sách URL (mỗi dòng 1 URL)", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        int added = 0, skipped = 0;
        for (String line : area.getText().split("\\r?\\n")) {
            String url = line.trim();
            if (url.isEmpty()) continue;
            TrackedLink t = new TrackedLink();
            t.setUrl(url);
            t.setPlacedDate(LocalDate.now());
            if (dao.insert(t)) added++; else skipped++;
        }
        refresh();
        UiUtil.info(this, "Đã thêm " + added + " URL, bỏ qua " + skipped + " URL trùng/lỗi.");
    }

    private List<TrackedLink> selected() {
        List<TrackedLink> sel = new ArrayList<>();
        for (int v : table.getSelectedRows()) sel.add(current.get(table.convertRowIndexToModel(v)));
        return sel;
    }

    private void deleteSelected() {
        List<TrackedLink> sel = selected();
        if (sel.isEmpty()) {
            UiUtil.warn(this, "Hãy chọn ít nhất 1 dòng.");
            return;
        }
        if (!UiUtil.confirm(this, "Xoá " + sel.size() + " link khỏi danh sách theo dõi?")) return;
        for (TrackedLink t : sel) dao.delete(t.getId());
        refresh();
    }

    private void openLink() {
        List<TrackedLink> sel = selected();
        if (sel.isEmpty()) {
            UiUtil.warn(this, "Hãy chọn 1 dòng.");
            return;
        }
        UiUtil.openBrowser(this, sel.get(0).getUrl());
    }

    /** Kiểm tra song song (5 luồng); mỗi kết quả cập nhật trạng thái link và được lưu vào lịch sử kiểm tra. */
    private void runCheck(boolean all) {
        List<TrackedLink> targets = all ? new ArrayList<>(current) : selected();
        if (targets.isEmpty()) {
            UiUtil.warn(this, all ? "Danh sách theo dõi đang trống." : "Hãy chọn ít nhất 1 dòng.");
            return;
        }
        List<String> urls = new ArrayList<>();
        for (TrackedLink t : targets) urls.add(t.getUrl());
        String target = AppSettings.getTargetDomain();

        btnCheckAll.setEnabled(false);
        btnCheckSel.setEnabled(false);
        progress.setMaximum(urls.size());
        progress.setValue(0);
        progress.setVisible(true);

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                BatchCheckService.checkAll(urls, target, 5, (idx, res, done, total) -> {
                    historyDao.insert(res);
                    dao.updateStatus(targets.get(idx).getId(), res.getStatus(), res.getCheckedAt());
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
                btnCheckAll.setEnabled(true);
                btnCheckSel.setEnabled(true);
                refresh();
            }
        };
        worker.execute();
    }

    private void exportExcel() {
        if (table.getRowCount() == 0) {
            UiUtil.info(this, "Chưa có dữ liệu để xuất.");
            return;
        }
        String path = UiUtil.chooseSavePath(this, "Lưu danh sách Backlink Monitor", "backlink_monitor.xlsx");
        if (path == null) return;
        try {
            ExcelExporter.exportTable(table, "Backlink Monitor", path);
            UiUtil.info(this, "Đã xuất file thành công.");
        } catch (Exception ex) {
            UiUtil.error(this, "Lỗi khi xuất file: " + ex.getMessage());
        }
    }

    private class RowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
            Component comp = super.getTableCellRendererComponent(t, v, sel, focus, r, c);
            if (!sel) {
                int m = t.convertRowIndexToModel(r);
                String status = String.valueOf(model.getValueAt(m, 3));
                String warn = String.valueOf(model.getValueAt(m, 6));
                Color bg = Color.WHITE;
                if (CheckResult.STATUS_DOFOLLOW.equals(status)) bg = new Color(0xD5F5E3);
                else if (CheckResult.STATUS_NOFOLLOW.equals(status)) bg = new Color(0xFDEBD0);
                else if (CheckResult.STATUS_ERROR.equals(status)) bg = new Color(0xFADBD8);
                if (!warn.isEmpty()) bg = new Color(0xF5B7B1);
                comp.setBackground(bg);
                comp.setForeground(Color.BLACK);
            }
            return comp;
        }
    }
}
