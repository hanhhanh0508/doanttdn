package com.sapota.seo.ui;

import com.sapota.seo.dao.GapDomainDAO;
import com.sapota.seo.service.EmailService;
import com.sapota.seo.service.SchedulerService;
import com.sapota.seo.util.AppSettings;

import javax.swing.*;
import java.awt.*;

/** Tab Cài đặt: chỉnh trọng số công thức điểm ưu tiên và domain đích mặc định. */
public class SettingsPanel extends JPanel implements Refreshable {

    private final JTextField txtWeightAs = new JTextField(8);
    private final JTextField txtWeightComp = new JTextField(8);
    private final JTextField txtTarget = new JTextField(20);
    private final JLabel lblFormula = new JLabel();

    // Kiểm tra tự động + email cảnh báo
    private final JCheckBox chkSchedule = new JCheckBox("Tự động kiểm tra backlink mỗi ngày");
    private final JTextField txtTime = new JTextField(6);
    private final JCheckBox chkEmail = new JCheckBox("Gửi email khi phát hiện mất/đổi link");
    private final JTextField txtHost = new JTextField(18);
    private final JTextField txtPort = new JTextField(6);
    private final JTextField txtSmtpUser = new JTextField(22);
    private final JPasswordField txtSmtpPass = new JPasswordField(22);
    private final JTextField txtTo = new JTextField(22);
    private final JLabel lblLastRun = new JLabel(" ");

    public SettingsPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Công thức điểm ưu tiên & domain đích"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0; form.add(new JLabel("Trọng số AS tối đa:"), c);
        c.gridx = 1; form.add(txtWeightAs, c);
        c.gridx = 0; c.gridy = 1; form.add(new JLabel("Trọng số số đối thủ:"), c);
        c.gridx = 1; form.add(txtWeightComp, c);
        c.gridx = 0; c.gridy = 2; form.add(new JLabel("Domain đích mặc định:"), c);
        c.gridx = 1; form.add(txtTarget, c);
        c.gridx = 0; c.gridy = 3; c.gridwidth = 2; form.add(lblFormula, c);

        JButton btnSave = new JButton("Lưu và tính lại điểm ưu tiên");
        btnSave.addActionListener(e -> save());
        c.gridy = 4; form.add(btnSave, c);

        JTextArea help = new JTextArea(
                "Điểm ưu tiên = (AS tối đa × trọng số AS) + (số đối thủ × trọng số đối thủ).\n"
                + "Tăng trọng số đối thủ nếu muốn ưu tiên domain được nhiều đối thủ cùng liên kết;\n"
                + "tăng trọng số AS nếu muốn ưu tiên domain có uy tín cao.\n"
                + "Sau khi lưu, toàn bộ domain Backlink Gap được tính lại và xếp hạng lại.\n"
                + "Domain đích mặc định được dùng làm giá trị khởi tạo cho Dofollow Checker.");
        help.setEditable(false);
        help.setOpaque(false);
        help.setBorder(BorderFactory.createEmptyBorder(10, 4, 0, 4));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        form.setAlignmentX(Component.LEFT_ALIGNMENT);
        help.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel auto = buildAutoPanel();
        auto.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(form);
        content.add(help);
        content.add(Box.createVerticalStrut(10));
        content.add(auto);

        JPanel top = new JPanel(new BorderLayout());
        top.add(content, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(top);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildAutoPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Kiểm tra backlink tự động & email cảnh báo"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 8, 5, 8);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; p.add(chkSchedule, c);
        c.gridwidth = 1;
        c.gridy = 1; c.gridx = 0; p.add(new JLabel("Giờ chạy hằng ngày (HH:mm):"), c);
        c.gridx = 1; p.add(txtTime, c);
        c.gridy = 2; c.gridx = 0; c.gridwidth = 2; p.add(chkEmail, c);
        c.gridwidth = 1;
        c.gridy = 3; c.gridx = 0; p.add(new JLabel("SMTP host:"), c);
        c.gridx = 1; p.add(txtHost, c);
        c.gridy = 4; c.gridx = 0; p.add(new JLabel("SMTP port (587 hoặc 465):"), c);
        c.gridx = 1; p.add(txtPort, c);
        c.gridy = 5; c.gridx = 0; p.add(new JLabel("Email gửi (tài khoản SMTP):"), c);
        c.gridx = 1; p.add(txtSmtpUser, c);
        c.gridy = 6; c.gridx = 0; p.add(new JLabel("Mật khẩu ứng dụng:"), c);
        c.gridx = 1; p.add(txtSmtpPass, c);
        c.gridy = 7; c.gridx = 0; p.add(new JLabel("Email nhận cảnh báo:"), c);
        c.gridx = 1; p.add(txtTo, c);

        JButton btnSave = new JButton("Lưu cấu hình tự động");
        JButton btnTest = new JButton("Gửi email thử");
        JButton btnRun = new JButton("Chạy kiểm tra ngay");
        btnSave.addActionListener(e -> saveAuto());
        btnTest.addActionListener(e -> sendTestEmail());
        btnRun.addActionListener(e -> runNow(btnRun));
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btns.add(btnSave);
        btns.add(btnTest);
        btns.add(btnRun);
        c.gridy = 8; c.gridx = 0; c.gridwidth = 2; p.add(btns, c);

        JLabel note = new JLabel("<html><i>Lịch chỉ chạy khi ứng dụng đang mở. Gmail cần bật xác minh 2 bước và tạo \"Mật khẩu ứng dụng\".<br>"
                + "Mật khẩu SMTP được lưu trong ~/.sapota-seo-dashboard/settings.properties trên máy này.</i></html>");
        note.setForeground(Color.GRAY);
        c.gridy = 9; p.add(note, c);
        c.gridy = 10; p.add(lblLastRun, c);
        return p;
    }

    private void saveAuto() {
        if (SchedulerService.parseTime(txtTime.getText()) == null) {
            UiUtil.warn(this, "Giờ chạy không hợp lệ. Hãy nhập dạng HH:mm, ví dụ 08:00.");
            return;
        }
        int port = UiUtil.parseIntSafe(txtPort.getText(), -1);
        if (port <= 0 || port > 65535) {
            UiUtil.warn(this, "SMTP port không hợp lệ.");
            return;
        }
        try {
            AppSettings.saveSchedule(chkSchedule.isSelected(), txtTime.getText(), chkEmail.isSelected(),
                    txtHost.getText(), port, txtSmtpUser.getText(), new String(txtSmtpPass.getPassword()), txtTo.getText());
            SchedulerService.getInstance().restartFromSettings();
            UiUtil.info(this, chkSchedule.isSelected()
                    ? "Đã lưu. Backlink sẽ được kiểm tra tự động mỗi ngày lúc " + txtTime.getText().trim() + "."
                    : "Đã lưu. Kiểm tra tự động đang TẮT.");
        } catch (Exception ex) {
            UiUtil.error(this, "Không lưu được cấu hình: " + ex.getMessage());
        }
    }

    /** Gửi thử bằng đúng thông tin đang nhập trên form (đã lưu trước để EmailService đọc được). */
    private void sendTestEmail() {
        saveAuto();
        if (!EmailService.isConfigured()) {
            UiUtil.warn(this, "Hãy tick 'Gửi email...' và điền đủ host, tài khoản, mật khẩu, email nhận.");
            return;
        }
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                try {
                    new EmailService().send("[SEO Dashboard] Email thử", "Nếu bạn đọc được email này, cấu hình SMTP đã đúng.");
                    return null;
                } catch (Exception e) {
                    return e.getMessage();
                }
            }

            @Override
            protected void done() {
                try {
                    String err = get();
                    if (err == null) UiUtil.info(SettingsPanel.this, "Đã gửi email thử tới " + AppSettings.getEmailTo() + ".");
                    else UiUtil.error(SettingsPanel.this, "Gửi email thất bại: " + err);
                } catch (Exception ex) {
                    UiUtil.error(SettingsPanel.this, "Gửi email thất bại: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void runNow(JButton btn) {
        btn.setEnabled(false);
        lblLastRun.setText("Đang kiểm tra...");
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return SchedulerService.getInstance().runNow();
            }

            @Override
            protected void done() {
                btn.setEnabled(true);
                lblLastRun.setText("Lần chạy gần nhất: " + SchedulerService.getInstance().getLastRunMessage());
            }
        }.execute();
    }

    @Override
    public void refresh() {
        txtWeightAs.setText(String.valueOf(AppSettings.getWeightAs()));
        txtWeightComp.setText(String.valueOf(AppSettings.getWeightCompetitor()));
        txtTarget.setText(AppSettings.getTargetDomain());
        lblFormula.setText("Công thức hiện tại: điểm = AS × " + AppSettings.getWeightAs()
                + " + số đối thủ × " + AppSettings.getWeightCompetitor());
        chkSchedule.setSelected(AppSettings.isScheduleEnabled());
        txtTime.setText(AppSettings.getScheduleTime());
        chkEmail.setSelected(AppSettings.isEmailEnabled());
        txtHost.setText(AppSettings.getSmtpHost());
        txtPort.setText(String.valueOf(AppSettings.getSmtpPort()));
        txtSmtpUser.setText(AppSettings.getSmtpUser());
        txtSmtpPass.setText(AppSettings.getSmtpPassword());
        txtTo.setText(AppSettings.getEmailTo());
        lblLastRun.setText("Lần chạy gần nhất: " + SchedulerService.getInstance().getLastRunMessage());
    }

    private void save() {
        try {
            double wAs = Double.parseDouble(txtWeightAs.getText().trim().replace(',', '.'));
            double wComp = Double.parseDouble(txtWeightComp.getText().trim().replace(',', '.'));
            if (wAs < 0 || wComp < 0) throw new NumberFormatException();
            String target = UiUtil.cleanDomain(txtTarget.getText());
            if (target.isEmpty()) {
                UiUtil.warn(this, "Domain đích không được để trống.");
                return;
            }
            AppSettings.save(wAs, wComp, target);
            int n = new GapDomainDAO().recalculateAll();
            refresh();
            UiUtil.info(this, "Đã lưu cài đặt và tính lại điểm cho " + n + " domain.");
        } catch (NumberFormatException ex) {
            UiUtil.warn(this, "Trọng số phải là số không âm (ví dụ 1.0 hoặc 5).");
        } catch (Exception ex) {
            UiUtil.error(this, "Không lưu được cài đặt: " + ex.getMessage());
        }
    }
}
