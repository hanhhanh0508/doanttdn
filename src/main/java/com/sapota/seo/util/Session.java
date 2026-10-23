package com.sapota.seo.util;

import com.sapota.seo.model.User;

/** Lưu người dùng đang đăng nhập trong suốt phiên làm việc của ứng dụng. */
public final class Session {

    private static volatile User current;

    private Session() {
    }

    public static void login(User user) {
        current = user;
    }

    public static void logout() {
        current = null;
    }

    public static User getCurrentUser() {
        return current;
    }

    public static boolean isLoggedIn() {
        return current != null;
    }

    public static boolean isAdmin() {
        User u = current;
        return u != null && u.isAdmin();
    }
}
