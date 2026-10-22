package com.sapota.seo.ui;

import com.sapota.seo.dao.GapDomainDAO;
import com.sapota.seo.util.AppSettings;

import javax.swing.*;
import java.awt.*;

/** Tab Cài đặt: chỉnh trọng số công thức điểm ưu tiên và domain đích mặc định. */
public class SettingsPanel extends JPanel implements Refreshable {

    private final JTextField txtWeightAs = new JTextField(8);
    private final JTextField txtWeightComp = new JTextField(8);
    private final JTextField txtTarget = new JTextField(20);
    private final JLabel lblFormula = new JLabel();

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

        JPanel top = new JPanel(new BorderLayout());
        top.add(form, BorderLayout.NORTH);
        top.add(help, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
        refresh();
    }

    @Override
    public void refresh() {
        txtWeightAs.setText(String.valueOf(AppSettings.getWeightAs()));
        txtWeightComp.setText(String.valueOf(AppSettings.getWeightCompetitor()));
        txtTarget.setText(AppSettings.getTargetDomain());
        lblFormula.setText("Công thức hiện tại: điểm = AS × " + AppSettings.getWeightAs()
                + " + số đối thủ × " + AppSettings.getWeightCompetitor());
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
