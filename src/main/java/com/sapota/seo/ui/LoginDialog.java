package com.sapota.seo.ui;

import com.sapota.seo.model.User;
import com.sapota.seo.service.AuthService;

import javax.swing.*;
import java.awt.*;

/** Hộp thoại đăng nhập hiển thị trước khi vào màn hình chính. */
public class LoginDialog extends JDialog {

    private static final int MAX_ATTEMPTS = 5;

    private final AuthService auth = new AuthService();
    private final JTextField txtUser = new JTextField(18);
    private final JPasswordField txtPass = new JPasswordField(18);
    private final JLabel lblError = new JLabel(" ");
    private int attempts = 0;
    private User loggedIn;

    public LoginDialog() {
        super((Frame) null, "Đăng nhập - SapotaCorp SEO Dashboard", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 8, 24));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;

        JLabel title = new JLabel("SapotaCorp SEO Dashboard");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; form.add(title, c);

        c.gridwidth = 1;
        c.gridy = 1; c.gridx = 0; form.add(new JLabel("Tên đăng nhập:"), c);
        c.gridx = 1; form.add(txtUser, c);
        c.gridy = 2; c.gridx = 0; form.add(new JLabel("Mật khẩu:"), c);
        c.gridx = 1; form.add(txtPass, c);

        lblError.setForeground(new Color(0xC0392B));
        c.gridy = 3; c.gridx = 0; c.gridwidth = 2; form.add(lblError, c);

        JButton btnLogin = new JButton("Đăng nhập");
        JButton btnCancel = new JButton("Thoát");
        btnLogin.addActionListener(e -> doLogin());
        btnCancel.addActionListener(e -> dispose());
        txtPass.addActionListener(e -> doLogin());
        getRootPane().setDefaultButton(btnLogin);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(btnCancel);
        buttons.add(btnLogin);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
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
    }

    /** Hiển thị hộp thoại (chặn tới khi đóng) và trả về user đã đăng nhập, hoặc null nếu huỷ. */
    public static User showLogin() {
        LoginDialog d = new LoginDialog();
        d.setVisible(true);
        return d.loggedIn;
    }
}
