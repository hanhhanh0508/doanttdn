package com.sapota.seo.ui;

import com.sapota.seo.dao.OutreachDAO;
import com.sapota.seo.model.OutreachRecord;
import com.sapota.seo.util.AppSettings;
import com.sapota.seo.util.ExcelExporter;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tab Outreach Tracker: theo dõi tiến độ liên hệ xin backlink từng domain
 * (giống một CRM thu nhỏ: trạng thái, người liên hệ, ngày hẹn follow-up, ghi chú).
 */
public class OutreachPanel extends JPanel implements Refreshable {

    private final OutreachDAO dao = new OutreachDAO();
    private final DefaultTableModel model = UiUtil.newModel(
            new String[]{"Domain", "Trạng thái", "Liên hệ", "Liên hệ gần nhất", "Hẹn follow-up", "Nhắc việc", "Ghi chú"}, null);
    private final JTable table = new JTable(model);
    private final JComboBox<String> cbStatus = new JComboBox<>();
    private final JTextField txtSearch = new JTextField(16);
    private final JLabel lblSummary = new JLabel(" ");
    private List<OutreachRecord> current = new ArrayList<>();

    public OutreachPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        cbStatus.addItem("Tất cả");
        for (String s : OutreachRecord.STATUSES) cbStatus.addItem(s);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        bar.add(new JLabel("Trạng thái:"));
        bar.add(cbStatus);
        bar.add(new JLabel("Tìm kiếm:"));
        bar.add(txtSearch);
        JButton btnFilter = new JButton("Lọc / Làm mới");
        btnFilter.addActionListener(e -> refresh());
        txtSearch.addActionListener(e -> refresh());
        bar.add(btnFilter);
        add(bar, BorderLayout.NORTH);

        table.setRowHeight(24);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(6).setPreferredWidth(300);
        table.setDefaultRenderer(Object.class, new RowRenderer());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) editSelected();
            }

            @Override
            public void mousePressed(MouseEvent e) { popup(e); }

            @Override
            public void mouseReleased(MouseEvent e) { popup(e); }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(lblSummary, BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.add(button("Thêm domain", e -> addNew()));
        right.add(button("Sửa", e -> editSelected()));
        right.add(button("Soạn email mẫu", e -> composeEmail()));
        right.add(button("Mở website", e -> openSite()));
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
        String status = "Tất cả".equals(cbStatus.getSelectedItem()) ? null : (String) cbStatus.getSelectedItem();
        current = dao.findAll(status, txtSearch.getText().trim());
        model.setRowCount(0);
        int overdue = 0;
        for (OutreachRecord r : current) {
            if ("Quá hạn".equals(r.getReminder())) overdue++;
            model.addRow(new Object[]{r.getDomain(), r.getStatus(), r.getContact(),
                    UiUtil.formatDate(r.getLastContact()), UiUtil.formatDate(r.getFollowUp()),
                    r.getReminder(), r.getNote()});
        }
        Map<String, Integer> counts = dao.countByStatus();
        StringBuilder sb = new StringBuilder("Hiển thị " + current.size() + " | ");
        for (Map.Entry<String, Integer> e : counts.entrySet()) sb.append(e.getKey()).append(": ").append(e.getValue()).append("  ");
        sb.append("| Quá hạn follow-up: ").append(overdue);
        lblSummary.setText(sb.toString());
    }

    private OutreachRecord selected() {
        int v = table.getSelectedRow();
        if (v < 0) return null;
        return current.get(table.convertRowIndexToModel(v));
    }

    private void popup(MouseEvent e) {
        if (!e.isPopupTrigger()) return;
        int row = table.rowAtPoint(e.getPoint());
        if (row >= 0) table.setRowSelectionInterval(row, row);
        if (selected() == null) return;
        JPopupMenu menu = new JPopupMenu();
        for (String s : OutreachRecord.STATUSES) {
            JMenuItem item = new JMenuItem("Đổi trạng thái → " + s);
            item.addActionListener(ev -> {
                OutreachRecord r = selected();
                if (r != null) {
                    dao.updateStatus(r.getId(), s);
                    refresh();
                }
            });
            menu.add(item);
        }
        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void addNew() {
        OutreachRecord r = showDialog(null);
        if (r == null) return;
        if (dao.insert(r)) refresh();
        else UiUtil.warn(this, "Domain \"" + r.getDomain() + "\" đã có trong danh sách Outreach (hoặc có lỗi lưu).");
    }

    private void editSelected() {
        OutreachRecord old = selected();
        if (old == null) {
            UiUtil.warn(this, "Hãy chọn 1 dòng để sửa.");
            return;
        }
        OutreachRecord r = showDialog(old);
        if (r == null) return;
        r.setId(old.getId());
        if (dao.update(r)) refresh();
        else UiUtil.error(this, "Không cập nhật được.");
    }

    private void deleteSelected() {
        OutreachRecord r = selected();
        if (r == null) {
            UiUtil.warn(this, "Hãy chọn 1 dòng để xoá.");
            return;
        }
        if (!UiUtil.confirm(this, "Xoá \"" + r.getDomain() + "\" khỏi danh sách Outreach?")) return;
        dao.delete(r.getId());
        refresh();
    }

    private void openSite() {
        OutreachRecord r = selected();
        if (r == null) {
            UiUtil.warn(this, "Hãy chọn 1 dòng.");
            return;
        }
        UiUtil.openBrowser(this, r.getDomain());
    }

    /** Hộp thoại thêm/sửa; trả về null nếu huỷ hoặc dữ liệu không hợp lệ. */
    private OutreachRecord showDialog(OutreachRecord old) {
        JTextField fDomain = new JTextField(old == null ? "" : old.getDomain(), 24);
        fDomain.setEditable(old == null);
        JComboBox<String> fStatus = new JComboBox<>(OutreachRecord.STATUSES);
        if (old != null) fStatus.setSelectedItem(old.getStatus());
        JTextField fContact = new JTextField(old == null || old.getContact() == null ? "" : old.getContact(), 24);
        JTextField fLast = new JTextField(old == null ? "" : UiUtil.formatDate(old.getLastContact()), 24);
        JTextField fFollow = new JTextField(old == null ? "" : UiUtil.formatDate(old.getFollowUp()), 24);
        JTextArea fNote = new JTextArea(old == null || old.getNote() == null ? "" : old.getNote(), 3, 24);
        fNote.setLineWrap(true);

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Domain:")); form.add(fDomain);
        form.add(new JLabel("Trạng thái:")); form.add(fStatus);
        form.add(new JLabel("Người/email liên hệ:")); form.add(fContact);
        form.add(new JLabel("Liên hệ gần nhất (dd/MM/yyyy):")); form.add(fLast);
        form.add(new JLabel("Hẹn follow-up (dd/MM/yyyy):")); form.add(fFollow);
        form.add(new JLabel("Ghi chú:")); form.add(new JScrollPane(fNote));

        if (JOptionPane.showConfirmDialog(this, form, old == null ? "Thêm domain Outreach" : "Sửa Outreach",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return null;

        String domain = UiUtil.cleanDomain(fDomain.getText());
        if (domain.isEmpty()) {
            UiUtil.warn(this, "Domain không được để trống.");
            return null;
        }
        try {
            LocalDate last = UiUtil.parseDate(fLast.getText());
            LocalDate follow = UiUtil.parseDate(fFollow.getText());
            OutreachRecord r = new OutreachRecord();
            r.setDomain(domain);
            r.setStatus((String) fStatus.getSelectedItem());
            r.setContact(fContact.getText().trim());
            r.setNote(fNote.getText().trim());
            r.setLastContact(last);
            r.setFollowUp(follow);
            return r;
        } catch (DateTimeParseException ex) {
            UiUtil.warn(this, "Ngày không hợp lệ. Hãy nhập dạng dd/MM/yyyy, ví dụ 25/10/2026.");
            return null;
        }
    }

    /** Sinh email xin đăng/đặt link (tiếng Anh) điền sẵn domain và người nhận để copy gửi đi. */
    private void composeEmail() {
        OutreachRecord r = selected();
        if (r == null) {
            UiUtil.warn(this, "Hãy chọn 1 domain để soạn email.");
            return;
        }
        String site = AppSettings.getTargetDomain();
        String name = r.getContact() == null || r.getContact().isEmpty() || r.getContact().contains("@")
                ? "there" : r.getContact();
        String text = "Subject: Collaboration opportunity between " + r.getDomain() + " and SapotaCorp\n\n"
                + "Hi " + name + ",\n\n"
                + "I'm reaching out from SapotaCorp (https://" + site + "), a Vietnam-based software company "
                + "providing dedicated engineering teams (Embedded Engineer and Fixed-scope services) for clients in "
                + "E-commerce, Fintech, Blockchain and AI & Automation.\n\n"
                + "While researching resources in our industry, I found " + r.getDomain() + " and believe our readers "
                + "and yours share a lot of interest. We would love to be listed on your website, or contribute a short "
                + "guest article about software outsourcing, if that is something you accept.\n\n"
                + "Could you let me know the best way to submit our company profile? I'm happy to provide any details you need.\n\n"
                + "Thank you for your time, and I look forward to hearing from you.\n\n"
                + "Best regards,\n[Your name]\nSapotaCorp\nhttps://" + site + "\n";

        JTextArea area = new JTextArea(text, 18, 60);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        Object[] options = {"Copy vào Clipboard", "Đóng"};
        int choice = JOptionPane.showOptionDialog(this, new JScrollPane(area), "Email mẫu gửi " + r.getDomain(),
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (choice == 0) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(area.getText()), null);
        }
    }

    private void exportExcel() {
        if (table.getRowCount() == 0) {
            UiUtil.info(this, "Chưa có dữ liệu để xuất.");
            return;
        }
        String path = UiUtil.chooseSavePath(this, "Lưu danh sách Outreach", "outreach_tracker.xlsx");
        if (path == null) return;
        try {
            ExcelExporter.exportTable(table, "Outreach", path);
            UiUtil.info(this, "Đã xuất file thành công.");
        } catch (Exception ex) {
            UiUtil.error(this, "Lỗi khi xuất file: " + ex.getMessage());
        }
    }

    /** Tô màu theo trạng thái; dòng quá hạn follow-up tô đỏ đậm. */
    private class RowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
            Component comp = super.getTableCellRendererComponent(t, v, sel, focus, r, c);
            if (!sel) {
                int m = t.convertRowIndexToModel(r);
                String status = String.valueOf(model.getValueAt(m, 1));
                String reminder = String.valueOf(model.getValueAt(m, 5));
                Color bg = Color.WHITE;
                if (OutreachRecord.WON.equals(status)) bg = new Color(0xD5F5E3);
                else if (OutreachRecord.REJECTED.equals(status)) bg = new Color(0xEAECEE);
                else if (OutreachRecord.NEGOTIATING.equals(status)) bg = new Color(0xFCF3CF);
                else if (OutreachRecord.CONTACTED.equals(status)) bg = new Color(0xEBF5FB);
                if ("Quá hạn".equals(reminder)) bg = new Color(0xFADBD8);
                comp.setBackground(bg);
                comp.setForeground(Color.BLACK);
            }
            return comp;
        }
    }
}
