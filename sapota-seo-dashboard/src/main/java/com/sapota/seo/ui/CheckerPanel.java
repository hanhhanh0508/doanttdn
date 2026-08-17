package com.sapota.seo.ui;

import com.sapota.seo.dao.CheckResultDAO;
import com.sapota.seo.model.CheckResult;
import com.sapota.seo.service.DofollowCheckerService;
import com.sapota.seo.service.RegistrationPageFinderService;
import com.sapota.seo.util.CsvExporter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Màn hình Dofollow Checker (tương ứng wireframe Hình 4.5 trong báo cáo).
 * Cho phép người dùng dán danh sách URL, kiểm tra hàng loạt và xem kết quả.
 *
 * Việc kiểm tra được chạy trong SwingWorker (luồng nền) để không làm treo
 * giao diện khi kiểm tra nhiều URL cùng lúc.
 */
public class CheckerPanel extends JPanel {

    private final CheckResultDAO checkResultDAO = new CheckResultDAO();

    private JTextField txtTargetDomain;
    private JTextArea txtUrls;
    private JButton btnCheck;
    private JProgressBar progressBar;
    private DefaultTableModel tableModel;
    private JTable table;

    // Phần dò tìm trang đăng ký
    private JTextField txtRegDomain;
    private JTextArea txtRegResult;
    private JButton btnFindRegistration;

    // Phần kiểm tra redirect (đổi tên/đổi domain)
    private JTextField txtRedirectDomain;
    private JTextArea txtRedirectResult;
    private JButton btnCheckRedirect;

    public CheckerPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topArea = new JPanel();
        topArea.setLayout(new BoxLayout(topArea, BoxLayout.Y_AXIS));
        topArea.add(buildRedirectCheckBar());
        topArea.add(buildQuickSearchBar());
        topArea.add(buildRegistrationFinderBar());
        topArea.add(buildInputArea());

        add(topArea, BorderLayout.NORTH);
        add(buildResultTable(), BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);
    }

    /**
     * Thanh "Kiểm tra domain có đổi tên / chuyển hướng không" — dùng khi
     * công ty đối thủ (hoặc domain gap) có thể đã đổi tên/đổi domain.
     * Nếu họ có đặt redirect từ domain cũ sang domain mới, app tự phát hiện
     * được domain mới đó. Nếu không có redirect thì không có cách tự động —
     * cần dùng mục 2 (Tìm kiếm Google) để tra tay tên công ty mới.
     */
    private JPanel buildRedirectCheckBar() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("1. Kiểm tra domain có bị đổi tên/chuyển hướng không"));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row.add(new JLabel("Domain cũ:"));
        txtRedirectDomain = new JTextField("arrowhitech.com", 20);
        row.add(txtRedirectDomain);

        btnCheckRedirect = new JButton("Kiểm tra domain");
        btnCheckRedirect.addActionListener(e -> runCheckRedirect());
        row.add(btnCheckRedirect);

        panel.add(row, BorderLayout.NORTH);

        txtRedirectResult = new JTextArea(2, 60);
        txtRedirectResult.setEditable(false);
        txtRedirectResult.setLineWrap(true);
        panel.add(new JScrollPane(txtRedirectResult), BorderLayout.CENTER);

        return panel;
    }

    private void runCheckRedirect() {
        String domain = txtRedirectDomain.getText().trim();
        if (domain.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập domain cần kiểm tra.",
                    "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnCheckRedirect.setEnabled(false);
        txtRedirectResult.setText("Đang kiểm tra " + domain + "...");

        SwingWorker<DofollowCheckerService.RedirectResult, Void> worker = new SwingWorker<>() {
            @Override
            protected DofollowCheckerService.RedirectResult doInBackground() {
                return new DofollowCheckerService().checkRedirect(domain);
            }

            @Override
            protected void done() {
                try {
                    DofollowCheckerService.RedirectResult r = get();
                    if (r.error != null) {
                        txtRedirectResult.setText("Không kiểm tra được: " + r.error
                                + " (có thể domain đã ngừng hoạt động hẳn, không còn trỏ đi đâu)");
                    } else if (r.redirected) {
                        txtRedirectResult.setText("Domain NÀY ĐÃ CHUYỂN HƯỚNG sang domain khác:\n"
                                + r.originalUrl + "  →  " + r.finalUrl
                                + "\n(có thể đây là domain/tên mới sau khi công ty đổi tên — em kiểm tra lại cho chắc)");
                    } else {
                        txtRedirectResult.setText("Domain KHÔNG chuyển hướng, vẫn đang trỏ về chính nó: " + r.finalUrl
                                + "\n(nếu công ty đã đổi tên mà không thấy redirect ở đây, họ không đặt redirect — "
                                + "cần dùng mục 2 bên dưới để tìm tên/domain mới bằng Google)");
                    }
                } catch (Exception ex) {
                    txtRedirectResult.setText("Lỗi: " + ex.getMessage());
                } finally {
                    btnCheckRedirect.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    /**
     * Thanh "Tìm kiếm nhanh" hỗ trợ bước tìm cơ hội backlink theo cách
     * nhân viên SEO vẫn làm thủ công: mở tab ẩn danh, gõ
     * "site:<domain> <từ khoá>" trên Google, rồi vào từng kết quả kiểm tra
     * dofollow/nofollow.
     *
     * App KHÔNG tự động cào (scrape) kết quả tìm kiếm của Google vì việc
     * này vi phạm điều khoản sử dụng của Google và rất dễ bị chặn IP.
     * Thay vào đó, nút bên dưới chỉ mở sẵn trình duyệt với đúng câu lệnh
     * tìm kiếm — người dùng vẫn tự bấm vào kết quả và copy URL như cách
     * làm thủ công, chỉ đỡ công gõ lại site:... mỗi lần.
     */
    private JPanel buildQuickSearchBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panel.setBorder(BorderFactory.createTitledBorder("2. Tìm kiếm nhanh cơ hội backlink (Google)"));

        panel.add(new JLabel("Domain (site:):"));
        JTextField txtDomain = new JTextField("accio.com", 14);
        panel.add(txtDomain);

        panel.add(new JLabel("Từ khoá:"));
        JTextField txtKeyword = new JTextField("savvycom", 14);
        panel.add(txtKeyword);

        JButton btnSearch = new JButton("Tìm trên Google");
        btnSearch.addActionListener(e -> openGoogleSearch(txtDomain.getText(), txtKeyword.getText()));
        panel.add(btnSearch);

        JLabel hint = new JLabel("(mở trình duyệt sẵn câu lệnh site:domain từ_khoá — em tự bấm kết quả rồi copy URL xuống mục 3)");
        hint.setFont(hint.getFont().deriveFont(Font.ITALIC, 11f));
        hint.setForeground(Color.GRAY);
        panel.add(hint);

        return panel;
    }

    private void openGoogleSearch(String domain, String keyword) {
        domain = domain.trim();
        keyword = keyword.trim();
        if (domain.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập domain cần tìm (ví dụ: accio.com).",
                    "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String query = "site:" + domain + (keyword.isEmpty() ? "" : " " + keyword);
        String url = "https://www.google.com/search?q=" +
                java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new java.net.URI(url));
            } else {
                JOptionPane.showMessageDialog(this,
                        "Máy không hỗ trợ tự mở trình duyệt. Copy link sau và dán vào trình duyệt:\n" + url,
                        "Không thể tự mở trình duyệt", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không mở được trình duyệt: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Thanh "Tìm trang đăng ký doanh nghiệp" — bước "tìm coi có trang đăng ký
     * trong enterpriseleague.com không" mà sếp yêu cầu. Việc này gọi trực
     * tiếp tới chính website đích (không qua Google) nên tự động hoá được
     * an toàn, không vi phạm điều khoản của ai cả.
     */
    private JPanel buildRegistrationFinderBar() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("3. Tìm trang đăng ký doanh nghiệp trên 1 domain"));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row.add(new JLabel("Domain:"));
        txtRegDomain = new JTextField("enterpriseleague.com", 20);
        row.add(txtRegDomain);

        btnFindRegistration = new JButton("Tìm trang đăng ký");
        btnFindRegistration.addActionListener(e -> runFindRegistration());
        row.add(btnFindRegistration);

        panel.add(row, BorderLayout.NORTH);

        txtRegResult = new JTextArea(3, 60);
        txtRegResult.setEditable(false);
        txtRegResult.setLineWrap(true);
        panel.add(new JScrollPane(txtRegResult), BorderLayout.CENTER);

        return panel;
    }

    private void runFindRegistration() {
        String domain = txtRegDomain.getText().trim();
        if (domain.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập domain cần tìm (ví dụ: enterpriseleague.com).",
                    "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnFindRegistration.setEnabled(false);
        txtRegResult.setText("Đang dò tìm trang đăng ký trên " + domain + "...");

        SwingWorker<List<RegistrationPageFinderService.FoundPage>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<RegistrationPageFinderService.FoundPage> doInBackground() {
                return new RegistrationPageFinderService().find(domain);
            }

            @Override
            protected void done() {
                try {
                    List<RegistrationPageFinderService.FoundPage> found = get();
                    if (found.isEmpty()) {
                        txtRegResult.setText("Không tìm thấy trang đăng ký nào trong các đường dẫn phổ biến. "
                                + "Có thể site này dùng đường dẫn khác — thử vào trang chủ để tìm thủ công.");
                    } else {
                        StringBuilder sb = new StringBuilder("Tìm thấy " + found.size() + " trang có thể là trang đăng ký:\n");
                        for (RegistrationPageFinderService.FoundPage p : found) {
                            sb.append("- ").append(p.url).append(" (HTTP ").append(p.statusCode).append(")\n");
                        }
                        txtRegResult.setText(sb.toString());
                    }
                } catch (Exception ex) {
                    txtRegResult.setText("Lỗi khi dò tìm: " + ex.getMessage());
                } finally {
                    btnFindRegistration.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private JPanel buildInputArea() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("4. Kiểm tra Dofollow/Nofollow"));

        JPanel targetRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        targetRow.add(new JLabel("Domain đích cần tìm liên kết tới:"));
        txtTargetDomain = new JTextField("sapotacorp.vn", 20);
        targetRow.add(txtTargetDomain);
        JLabel hint = new JLabel("(đổi thành domain đối thủ, ví dụ arrowhitech.com, để kiểm tra trước khi outreach)");
        hint.setFont(hint.getFont().deriveFont(Font.ITALIC, 11f));
        hint.setForeground(Color.GRAY);
        targetRow.add(hint);

        JPanel textAreaPanel = new JPanel(new BorderLayout(5, 5));
        textAreaPanel.add(new JLabel("Danh sách URL cần kiểm tra (mỗi dòng 1 URL):"), BorderLayout.NORTH);

        txtUrls = new JTextArea(5, 60);
        txtUrls.setLineWrap(true);
        textAreaPanel.add(new JScrollPane(txtUrls), BorderLayout.CENTER);

        panel.add(targetRow, BorderLayout.NORTH);
        panel.add(textAreaPanel, BorderLayout.CENTER);

        JPanel actionRow = new JPanel(new BorderLayout());
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        actionRow.add(progressBar, BorderLayout.CENTER);

        btnCheck = new JButton("Kiểm tra");
        btnCheck.addActionListener(e -> runCheck());
        actionRow.add(btnCheck, BorderLayout.EAST);

        panel.add(actionRow, BorderLayout.SOUTH);
        return panel;
    }

    private JScrollPane buildResultTable() {
        String[] columns = {"URL", "Trạng thái", "Thời gian kiểm tra", "Ghi chú"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.getColumnModel().getColumn(0).setPreferredWidth(280);
        table.getColumnModel().getColumn(3).setPreferredWidth(320);
        return new JScrollPane(table);
    }

    private JPanel buildBottomBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnExport = new JButton("Xuất kết quả (CSV)");
        btnExport.addActionListener(e -> exportCsv());
        panel.add(btnExport);
        return panel;
    }

    private void runCheck() {
        String[] rawLines = txtUrls.getText().split("\\r?\\n");
        java.util.List<String> urls = new java.util.ArrayList<>();
        for (String line : rawLines) {
            if (!line.trim().isEmpty()) {
                urls.add(line.trim());
            }
        }
        if (urls.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập ít nhất 1 URL.",
                    "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String targetDomain = txtTargetDomain.getText().trim();
        if (targetDomain.isEmpty()) {
            targetDomain = "sapotacorp.vn";
        }
        DofollowCheckerService checkerService = new DofollowCheckerService(targetDomain);

        btnCheck.setEnabled(false);
        tableModel.setRowCount(0);
        progressBar.setMaximum(urls.size());
        progressBar.setValue(0);

        // Chạy kiểm tra ở luồng nền để không treo giao diện (mỗi request HTTP
        // có thể mất vài giây), cập nhật tiến trình và bảng kết quả theo thời gian thực.
        SwingWorker<Void, CheckResult> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                int done = 0;
                for (String url : urls) {
                    CheckResult result = checkerService.check(url);
                    checkResultDAO.insert(result);
                    publish(result);
                    done++;
                    setProgress((int) (done * 100.0 / urls.size()));
                }
                return null;
            }

            @Override
            protected void process(List<CheckResult> chunks) {
                for (CheckResult r : chunks) {
                    tableModel.addRow(new Object[]{
                            r.getUrl(), r.getStatus(), r.getCheckedAtFormatted(), r.getNote()
                    });
                    progressBar.setValue(progressBar.getValue() + 1);
                }
            }

            @Override
            protected void done() {
                btnCheck.setEnabled(true);
                progressBar.setValue(progressBar.getMaximum());
            }
        };
        worker.execute();
    }

    private void exportCsv() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Chưa có kết quả để xuất.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu kết quả kiểm tra Dofollow");
        chooser.setSelectedFile(new File("dofollow_check_result.csv"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        try {
            CsvExporter.export(table, chooser.getSelectedFile().getAbsolutePath());
            JOptionPane.showMessageDialog(this, "Đã xuất kết quả thành công.",
                    "Xuất báo cáo", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xuất file: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
