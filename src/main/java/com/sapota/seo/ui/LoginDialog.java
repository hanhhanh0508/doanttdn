package com.sapota.seo.ui;

import com.sapota.seo.model.User;
import com.sapota.seo.service.AuthService;

import javax.swing.*;
import java.awt.*;

/** Hộp thoại đăng nhập hiển thị trước khi vào màn hình chính. */
public class LoginDialog extends JDialog {

    private static final int MAX_ATTEMPTS = 5;
    /** Kích thước ô nhập đặt cố định để không phụ thuộc cách từng Look&Feel tính độ rộng theo "số cột". */
    private static final int FIELD_WIDTH = 240;

    private final AuthService auth = new AuthService();
    private final JTextField txtUser = new JTextField();
    private final JPasswordField txtPass = new JPasswordField();
    private final JLabel lblError = new JLabel(" ");
    private int attempts = 0;
    private User loggedIn;

    public LoginDialog() {
        super((Frame) null, "Đăng nhập - SapotaCorp SEO Dashboard", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);   // luôn nổi lên trên, tránh bị che sau cửa sổ NetBeans/IDE
        setResizable(false);    // đặt trước pack() để Windows không đổi kích thước sau khi tính

        fixSize(txtUser);
        fixSize(txtPass);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 8, 24));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;

        JLabel title = new JLabel("SapotaCorp SEO Dashboard");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; form.add(title, c);

        c.gridwidth = 1;
        c.gridy = 1; c.gridx = 0; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Tên đăng nhập:"), c);
        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        form.add(txtUser, c);

        c.gridy = 2; c.gridx = 0; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Mật khẩu:"), c);
        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        form.add(txtPass, c);

        lblError.setForeground(new Color(0xC0392B));
        c.gridy = 3; c.gridx = 0; c.gridwidth = 2; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        form.add(lblError, c);

        JButton btnLogin = new JButton("Đăng nhập");
        JButton btnCancel = new JButton("Thoát");
        btnLogin.addActionListener(e -> doLogin());
        btnCancel.addActionListener(e -> dispose());
        txtUser.addActionListener(e -> txtPass.requestFocusInWindow()); // Enter ở ô tên -> nhảy xuống ô mật khẩu
        txtPass.addActionListener(e -> doLogin());
        getRootPane().setDefaultButton(btnLogin);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(btnCancel);
        buttons.add(btnLogin);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    private void fixSize(JTextField f) {
        int h = Math.max(28, f.getPreferredSize().height);
        Dimension d = new Dimension(FIELD_WIDTH, h);
        f.setPreferredSize(d);
        f.setMinimumSize(d);
    }

    private void doLogin() {
        User u = auth.login(txtUser.getText(), new String(txtPass.getPassword()));
        if (u != null) {
            loggedIn = u;
            dispose();
            return;
        }
        attempts++;
        txtPass.setText("");
        if (attempts >= MAX_ATTEMPTS) {
            JOptionPane.showMessageDialog(this, "Sai thông tin đăng nhập quá " + MAX_ATTEMPTS + " lần. Ứng dụng sẽ đóng.",
                    "Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        lblError.setText("Sai tên đăng nhập/mật khẩu hoặc tài khoản bị khoá (" + attempts + "/" + MAX_ATTEMPTS + ")");
        txtPass.requestFocusInWindow();
    }

    /** Hiển thị hộp thoại (chặn tới khi đóng) và trả về user đã đăng nhập, hoặc null nếu huỷ. */
    public static User showLogin() {
        LoginDialog d = new LoginDialog();
        d.setVisible(true);
        return d.loggedIn;
    }
}   