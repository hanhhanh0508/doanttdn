package com.sapota.seo.ui;

import com.sapota.seo.dao.GapDomainDAO;
import com.sapota.seo.dao.OutreachDAO;
import com.sapota.seo.model.GapDomain;
import com.sapota.seo.model.OutreachRecord;
import com.sapota.seo.service.ExcelImportService;
import com.sapota.seo.util.CsvExporter;
import com.sapota.seo.util.ExcelExporter;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Màn hình Dashboard Backlink Gap (tương ứng wireframe Hình 4.4 trong báo cáo).
 * Hiển thị bảng xếp hạng domain ưu tiên, lọc/tìm kiếm, thêm-sửa-xoá domain,
 * chuyển domain sang Outreach Tracker, nhập Excel và xuất CSV/Excel.
 */
public class DashboardPanel extends JPanel implements Refreshable {

    private static final int COL_LEVEL = 4;

    private final GapDomainDAO gapDomainDAO = new GapDomainDAO();
    private final OutreachDAO outreachDAO = new OutreachDAO();

    private JTextField txtMinAs;
    private JTextField txtMinCompetitor;
    private JTextField txtKeyword;
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel lblSummary;
    private PriorityChartPanel chartPanel;
    /** Danh sách domain đang hiển thị; chỉ số phần tử trùng với chỉ số dòng trong tableModel. */
    private List<GapDomain> current = new ArrayList<>();

    public DashboardPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildFilterBar(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        loadData();
    }

    @Override
    public void refresh() {
        loadData();
    }

    private JSplitPane buildCenter() {
        chartPanel = new PriorityChartPanel();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildTable(), chartPanel);
        splitPane.setResizeWeight(0.62);
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

        panel.add(new JLabel("Tìm domain/đối thủ:"));
        txtKeyword = new JTextField(12);
        txtKeyword.addActionListener(e -> loadData());
        panel.add(txtKeyword);

        JButton btnFilter = new JButton("Áp dụng lọc");
        btnFilter.addActionListener(e -> loadData());
        panel.add(btnFilter);

        JButton btnReset = new JButton("Đặt lại");
        btnReset.addActionListener(e -> {
            txtMinAs.setText("0");
            txtMinCompetitor.setText("0");
            txtKeyword.setText("");
            loadData();
        });
        panel.add(btnReset);

        JButton btnImport = new JButton("Nhập dữ liệu từ Excel...");
        btnImport.addActionListener(e -> importExcel());
        panel.add(btnImport);

        return panel;
    }

    private JScrollPane buildTable() {
        String[] columns = {"Domain", "MaxAS", "Số đối thủ", "Điểm ưu tiên", "Mức ưu tiên", "Outreach", "Đối thủ sở hữu"};
        tableModel = UiUtil.newModel(columns, new Class<?>[]{
                String.class, Integer.class, Integer.class, Double.class, String.class, String.class, String.class});
        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setAutoCreateRowSorter(true);
        int[] widths = {170, 60, 80, 100, 90, 110, 300};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.setDefaultRenderer(Object.class, new LevelRenderer());
        table.setDefaultRenderer(Integer.class, new LevelRenderer());
        table.setDefaultRenderer(Double.class, new LevelRenderer());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openWebsite();
            }

            @Override
            public void mousePressed(MouseEvent e) { showPopup(e); }

            @Override
            public void mouseReleased(MouseEvent e) { showPopup(e); }
        });
        return new JScrollPane(table);
    }

    private JPanel buildBottomBar() {
        JPanel panel = new JPanel(new BorderLayout());
        lblSummary = new JLabel(" ");
        panel.add(lblSummary, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        right.add(button("Thêm domain", e -> addDomain()));
        right.add(button("Sửa", e -> editDomain()));
        right.add(button("Xoá", e -> deleteDomain()));
        right.add(button("Mở website", e -> openWebsite()));
        right.add(button("Thêm vào Outreach", e -> addToOutreach()));
        right.add(button("Xuất CSV", e -> export(false)));
        right.add(button("Xuất Excel", e -> export(true)));
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    private JButton button(String text, java.awt.event.ActionListener l) {
        JButton b = new JButton(text);
        b.addActionListener(l);
        return b;
    }

    private void showPopup(MouseEvent e) {
        if (!e.isPopupTrigger()) return;
        int row = table.rowAtPoint(e.getPoint());
        if (row >= 0 && !table.isRowSelected(row)) table.setRowSelectionInterval(row, row);
        JPopupMenu menu = new JPopupMenu();
        JMenuItem open = new JMenuItem("Mở website");
        open.addActionListener(ev -> openWebsite());
        JMenuItem outreach = new JMenuItem("Thêm vào Outreach");
        outreach.addActionListener(ev -> addToOutreach());
        JMenuItem edit = new JMenuItem("Sửa");
        edit.addActionListener(ev -> editDomain());
        JMenuItem del = new JMenuItem("Xoá");
        del.addActionListener(ev -> deleteDomain());
        menu.add(open);
        menu.add(outreach);
        menu.addSeparator();
        menu.add(edit);
        menu.add(del);
        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void loadData() {
        int minAs = UiUtil.parseIntSafe(txtMinAs.getText(), 0);
        int minCompetitor = UiUtil.parseIntSafe(txtMinCompetitor.getText(), 0);

        current = gapDomainDAO.findAll(minAs, minCompetitor, txtKeyword.getText());
        Map<String, String> outreach = outreachDAO.findStatusMap();

        tableModel.setRowCount(0);
        int high = 0;
        for (GapDomain g : current) {
            if (g.getPriorityScore() >= 80) high++;
            tableModel.addRow(new Object[]{
                    g.getDomain(), g.getMaxAs(), g.getCompetitorCount(), g.getPriorityScore(),
                    g.getPriorityLevel(), outreach.getOrDefault(g.getDomain().toLowerCase(), "—"),
                    g.getCompetitors()
            });
        }
        lblSummary.setText("Tổng số domain: " + current.size() + " | Ưu tiên cao trở lên: " + high);
        chartPanel.setData(current);
    }

    private List<GapDomain> selectedDomains() {
        List<GapDomain> list = new ArrayList<>();
        for (int v : table.getSelectedRows()) list.add(current.get(table.convertRowIndexToModel(v)));
        return list;
    }

    private GapDomain selectedOne() {
        List<GapDomain> sel = selectedDomains();
        if (sel.isEmpty()) {
            UiUtil.warn(this, "Hãy chọn một domain trong bảng trước.");
            return null;
        }
        return sel.get(0);
    }

    private void addDomain() {
        GapDomain g = showDomainDialog(null);
        if (g == null) return;
        if (gapDomainDAO.existsByDomain(g.getDomain())) {
            UiUtil.warn(this, "Domain \"" + g.getDomain() + "\" đã có trong danh sách.");
            return;
        }
        gapDomainDAO.insert(g);
        loadData();
    }

    private void editDomain() {
        GapDomain old = selectedOne();
        if (old == null) return;
        GapDomain g = showDomainDialog(old);
        if (g == null) return;
        g.setId(old.getId());
        if (!gapDomainDAO.update(g)) UiUtil.error(this, "Không cập nhật được domain.");
        loadData();
    }

    private void deleteDomain() {
        List<GapDomain> sel = selectedDomains();
        if (sel.isEmpty()) {
            UiUtil.warn(this, "Hãy chọn ít nhất một domain để xoá.");
            return;
        }
        if (!UiUtil.confirm(this, "Xoá " + sel.size() + " domain đã chọn khỏi danh sách Backlink Gap?")) return;
        for (GapDomain g : sel) gapDomainDAO.delete(g.getId());
        loadData();
    }

    private void openWebsite() {
        List<GapDomain> sel = selectedDomains();
        if (!sel.isEmpty()) UiUtil.openBrowser(this, sel.get(0).getDomain());
    }

    private void addToOutreach() {
        List<GapDomain> sel = selectedDomains();
        if (sel.isEmpty()) {
            UiUtil.warn(this, "Hãy chọn ít nhất một domain để thêm vào Outreach.");
            return;
        }
        int added = 0;
        for (GapDomain g : sel) {
            OutreachRecord r = new OutreachRecord();
            r.setDomain(g.getDomain().toLowerCase());
            r.setNote("Từ Backlink Gap: AS " + g.getMaxAs() + ", " + g.getCompetitorCount() + " đối thủ");
            if (outreachDAO.insert(r)) added++;
        }
        loadData();
        UiUtil.info(this, "Đã thêm " + added + " domain vào Outreach Tracker"
                + (added < sel.size() ? " (" + (sel.size() - added) + " domain đã có sẵn)." : "."));
    }

    /** Hộp thoại thêm/sửa domain; điểm ưu tiên tự tính theo trọng số trong Cài đặt. */
    private GapDomain showDomainDialog(GapDomain old) {
        JTextField fDomain = new JTextField(old == null ? "" : old.getDomain(), 24);
        JTextField fAs = new JTextField(old == null ? "30" : String.valueOf(old.getMaxAs()), 24);
        JTextField fCount = new JTextField(old == null ? "1" : String.valueOf(old.getCompetitorCount()), 24);
        JTextArea fComp = new JTextArea(old == null || old.getCompetitors() == null ? "" : old.getCompetitors(), 3, 24);
        fComp.setLineWrap(true);

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Domain:")); form.add(fDomain);
        form.add(new JLabel("AS cao nhất (MaxAS):")); form.add(fAs);
        form.add(new JLabel("Số đối thủ đang có link:")); form.add(fCount);
        form.add(new JLabel("Đối thủ (cách nhau dấu phẩy):")); form.add(new JScrollPane(fComp));

        if (JOptionPane.showConfirmDialog(this, form, old == null ? "Thêm domain Backlink Gap" : "Sửa domain",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return null;

        String domain = UiUtil.cleanDomain(fDomain.getText());
        if (domain.isEmpty()) {
            UiUtil.warn(this, "Domain không được để trống.");
            return null;
        }
        try {
            int as = Integer.parseInt(fAs.getText().trim());
            int cnt = Integer.parseInt(fCount.getText().trim());
            if (as < 0 || as > 100 || cnt < 0) throw new NumberFormatException();
            return new GapDomain(domain, as, cnt, fComp.getText().trim(), GapDomain.calculatePriorityScore(as, cnt));
        } catch (NumberFormatException ex) {
            UiUtil.warn(this, "AS phải từ 0 đến 100 và số đối thủ phải là số nguyên không âm.");
            return null;
        }
    }

    private void importExcel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn file Excel Backlink Gap (sapota-gap-refdomains-as30-dofollow.xlsx)");
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        try {
            int count = new ExcelImportService().importGapDomains(file.getAbsolutePath());
            UiUtil.info(this, "Đã nhập " + count + " dòng dữ liệu thành công.");
            loadData();
        } catch (Exception ex) {
            UiUtil.error(this, "Lỗi khi đọc file Excel: " + ex.getMessage());
        }
    }

    private void export(boolean excel) {
        if (table.getRowCount() == 0) {
            UiUtil.info(this, "Chưa có dữ liệu để xuất.");
            return;
        }
        String path = UiUtil.chooseSavePath(this, "Lưu báo cáo Backlink Gap",
                excel ? "backlink_gap_report.xlsx" : "backlink_gap_report.csv");
        if (path == null) return;
        try {
            if (excel) ExcelExporter.exportTable(table, "Backlink Gap", path);
            else CsvExporter.export(table, path);
            UiUtil.info(this, "Đã xuất báo cáo thành công.");
        } catch (Exception ex) {
            UiUtil.error(this, "Lỗi khi xuất file: " + ex.getMessage());
        }
    }

    /** Tô màu dòng theo mức ưu tiên và định dạng cột điểm (1 chữ số thập phân). */
    private class LevelRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
            Object shown = v instanceof Double ? String.format("%.1f", (Double) v) : v;
            Component comp = super.getTableCellRendererComponent(t, shown, sel, focus, r, c);
            setHorizontalAlignment(v instanceof Number ? SwingConstants.RIGHT : SwingConstants.LEFT);
            if (!sel) {
                String level = String.valueOf(tableModel.getValueAt(t.convertRowIndexToModel(r), COL_LEVEL));
                Color bg = Color.WHITE;
                if ("Rất cao".equals(level)) bg = new Color(0xFADBD8);
                else if ("Cao".equals(level)) bg = new Color(0xFDEBD0);
                else if ("Trung bình".equals(level)) bg = new Color(0xFCF3CF);
                comp.setBackground(bg);
                comp.setForeground(Color.BLACK);
            }
            return comp;
        }
    }
}
