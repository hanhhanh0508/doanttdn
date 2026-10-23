package com.sapota.seo.service;

import com.sapota.seo.dao.UserDAO;
import com.sapota.seo.model.User;
import com.sapota.seo.util.PasswordHasher;
import com.sapota.seo.util.Session;

/** Xử lý đăng nhập, đổi mật khẩu và các quy tắc nghiệp vụ về tài khoản. */
public class AuthService {

    public static final int MIN_PASSWORD_LENGTH = 6;

    private final UserDAO dao;

    public AuthService() {
        this(new UserDAO());
    }

    public AuthService(UserDAO dao) {
        this.dao = dao;
    }

    /** Đăng nhập thành công thì lưu vào Session và trả về user; sai thông tin/tài khoản bị khoá thì trả null. */
    public User login(String username, String password) {
        if (username == null || username.isBlank() || password == null) return null;
        User u = dao.findByUsername(username.trim());
        if (u == null || !u.isActive()) return null;
        if (!PasswordHasher.verify(password, u.getPasswordHash())) return null;
        Session.login(u);
        return u;
    }

    public void logout() {
        Session.logout();
    }

    /** Kiểm tra mật khẩu mới hợp lệ; trả về thông báo lỗi hoặc null nếu hợp lệ. */
    public static String validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            return "Mật khẩu phải có ít nhất " + MIN_PASSWORD_LENGTH + " ký tự.";
        }
        return null;
    }

    public boolean createUser(String username, String password, String fullName, User.Role role) {
        if (username == null || username.isBlank() || validatePassword(password) != null) return false;
        return dao.insert(new User(username.trim(), PasswordHasher.hash(password), fullName, role));
    }

    public void changePassword(User user, String newPassword) {
        dao.updatePassword(user.getId(), PasswordHasher.hash(newPassword));
    }

    /**
     * Có được hạ quyền/khoá/xoá tài khoản này không? Không cho thao tác lên chính mình
     * và không cho làm mất quản trị viên cuối cùng (tránh tự khoá hệ thống).
     */
    public boolean canModifyAccess(User target, User actor) {
        if (target == null || actor == null) return false;
        if (target.getId() == actor.getId()) return false;
        return !(target.isAdmin() && target.isActive() && dao.countActiveAdmins() <= 1);
    }
}
