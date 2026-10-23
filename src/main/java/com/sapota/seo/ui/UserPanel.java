package com.sapota.seo.ui;

import com.sapota.seo.dao.UserDAO;
import com.sapota.seo.model.User;
import com.sapota.seo.service.AuthService;
import com.sapota.seo.util.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Tab Người dùng (chỉ Quản trị viên thấy): thêm tài khoản, đặt lại mật khẩu, đổi vai trò, khoá/mở, xoá. */
public class UserPanel extends JPanel implements Refreshable {

    private final UserDAO dao = new UserDAO();
    private final AuthService auth = new AuthService(dao);
    private final DefaultTableModel model = UiUtil.newModel(
            new String[]{"Tên đăng nhập", "Họ tên", "Vai trò", "Trạng thái"}, null);
    private final JTable table = new JTable(model);
    private List<User> current = new ArrayList<>();

    public UserPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel hint = new JLabel("Quản trị viên: toàn quyền (kể cả Cài đặt, Người dùng). Nhân viên: dùng các chức năng SEO, không vào Cài đặt/Người dùng.");
        hint.setForeground(Color.GRAY);
        add(hint, BorderLayout.NORTH);

        table.setRowHeight(24);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(button("Thêm tài khoản", e -> addUser()));
        buttons.add(button("Đặt lại mật khẩu", e -> resetPassword()));
        buttons.add(button("Đổi vai trò", e -> toggleRole()));
        buttons.add(button("Khoá / Mở khoá", e -> toggleActive()));
        buttons.add(button("Xoá", e -> deleteUser()));
        buttons.add(button("Đổi mật khẩu của tôi", e -> changeMyPassword()));
        add(buttons, BorderLayout.SOUTH);

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
        for (User u : current) {
            model.addRow(new Object[]{u.getUsername(), u.getFullName(), u.getRole().getLabel(),
                    u.isActive() ? "Hoạt động" : "Đã khoá"});
        }
    }

    private User selected() {
        int v = table.getSelectedRow();
        if (v < 0) {
            UiUtil.warn(this, "Hãy chọn 1 tài khoản.");
            return null;
        }
        return current.get(table.convertRowIndexToModel(v));
    }

    /** Hộp thoại nhập mật khẩu (ẩn ký tự); trả về null nếu huỷ hoặc mật khẩu không hợp lệ. */
    private String askNewPassword(String title) {
        JPasswordField p1 = new JPasswordField(16);
        JPasswordField p2 = new JPasswordField(16);
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Mật khẩu mới:")); form.add(p1);
        form.add(new JLabel("Nhập lại:")); form.add(p2);
        if (JOptionPane.showConfirmDialog(this, form, title, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return null;
        String a = new String(p1.getPassword());
        String b = new String(p2.getPassword());
        String err = AuthService.validatePassword(a);
        if (err != null) {
            UiUtil.warn(this, err);
            return null;
        }
        if (!a.equals(b)) {
            UiUtil.warn(this, "Hai mật khẩu nhập vào không giống nhau.");
            return null;
        }
        return a;
    }

    private void addUser() {
        JTextField fUser = new JTextField(16);
        JTextField fName = new JTextField(16);
        JPasswordField fPass = new JPasswordField(16);
        JComboBox<User.Role> cbRole = new JComboBox<>(User.Role.values());
        cbRole.setSelectedItem(User.Role.STAFF);
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Tên đăng nhập:")); form.add(fUser);
        form.add(new JLabel("Họ tên:")); form.add(fName);
        form.add(new JLabel("Mật khẩu (>= " + AuthService.MIN_PASSWORD_LENGTH + " ký tự):")); form.add(fPass);
        form.add(new JLabel("Vai trò:")); form.add(cbRole);
        if (JOptionPane.showConfirmDialog(this, form, "Thêm tài khoản", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;

        String err = AuthService.validatePassword(new String(fPass.getPassword()));
        if (fUser.getText().isBlank() || err != null) {
            UiUtil.warn(this, fUser.getText().isBlank() ? "Tên đăng nhập không được để trống." : err);
            return;
        }
        if (!auth.createUser(fUser.getText(), new String(fPass.getPassword()), fName.getText().trim(),
                (User.Role) cbRole.getSelectedItem())) {
            UiUtil.warn(this, "Không tạo được tài khoản (tên đăng nhập có thể đã tồn tại).");
        }
        refresh();
    }

    private void resetPassword() {
        User u = selected();
        if (u == null) return;
        String pw = askNewPassword("Đặt lại mật khẩu cho " + u.getUsername());
        if (pw == null) return;
        auth.changePassword(u, pw);
        UiUtil.info(this, "Đã đặt lại mật khẩu.");
    }

    private void changeMyPassword() {
        User me = Session.getCurrentUser();
        if (me == null) return;
        String pw = askNewPassword("Đổi mật khẩu của " + me.getUsername());
        if (pw == null) return;
        auth.changePassword(me, pw);
        UiUtil.info(this, "Đã đổi mật khẩu.");
    }

    private void toggleRole() {
        User u = selected();
        if (u == null) return;
        if (!auth.canModifyAccess(u, Session.getCurrentUser())) {
            UiUtil.warn(this, "Không thể đổi vai trò của chính mình hoặc của quản trị viên cuối cùng.");
            return;
        }
        dao.updateRole(u.getId(), u.isAdmin() ? User.Role.STAFF : User.Role.ADMIN);
        refresh();
    }

    private void toggleActive() {
        User u = selected();
        if (u == null) return;
        if (!auth.canModifyAccess(u, Session.getCurrentUser())) {
            UiUtil.warn(this, "Không thể khoá chính mình hoặc quản trị viên cuối cùng.");
            return;
        }
        dao.updateActive(u.getId(), !u.isActive());
        refresh();
    }

    private void deleteUser() {
        User u = selected();
        if (u == null) return;
        if (!auth.canModifyAccess(u, Session.getCurrentUser())) {
            UiUtil.warn(this, "Không thể xoá chính mình hoặc quản trị viên cuối cùng.");
            return;
        }
        if (!UiUtil.confirm(this, "Xoá tài khoản " + u.getUsername() + "?")) return;
        dao.delete(u.getId());
        refresh();
    }
}
