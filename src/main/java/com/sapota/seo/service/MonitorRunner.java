package com.sapota.seo.service;

import com.sapota.seo.dao.CheckResultDAO;
import com.sapota.seo.dao.TrackedLinkDAO;
import com.sapota.seo.model.CheckResult;
import com.sapota.seo.model.TrackedLink;
import com.sapota.seo.util.AppSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * Chạy 1 lượt kiểm tra toàn bộ backlink đang theo dõi (không cần giao diện):
 * kiểm tra song song -> cập nhật trạng thái + lưu lịch sử -> gửi email nếu có link bị mất.
 * Được gọi bởi SchedulerService (tự động hằng ngày) và nút "Chạy kiểm tra ngay" trong Cài đặt.
 */
public class MonitorRunner {

    /** Kết quả tóm tắt của 1 lượt chạy. */
    public static class Summary {
        public final int checked;
        public final int dofollow;
        public final int lost;
        public final boolean emailSent;
        public final String emailError;

        public Summary(int checked, int dofollow, int lost, boolean emailSent, String emailError) {
            this.checked = checked;
            this.dofollow = dofollow;
            this.lost = lost;
            this.emailSent = emailSent;
            this.emailError = emailError;
        }

        @Override
        public String toString() {
            String mail = emailSent ? ", đã gửi email cảnh báo"
                    : (emailError != null ? ", gửi email lỗi: " + emailError : "");
            return "Đã kiểm tra " + checked + " link: " + dofollow + " Dofollow, " + lost + " bị mất/đổi" + mail;
        }
    }

    private final TrackedLinkDAO trackedDao = new TrackedLinkDAO();
    private final CheckResultDAO historyDao = new CheckResultDAO();

    public Summary runOnce() {
        List<TrackedLink> targets = trackedDao.findAll();
        if (targets.isEmpty()) return new Summary(0, 0, 0, false, null);

        List<String> urls = new ArrayList<>();
        for (TrackedLink t : targets) urls.add(t.getUrl());

        BatchCheckService.checkAll(urls, AppSettings.getTargetDomain(), 5, (idx, res, done, total) -> {
            historyDao.insert(res);
            trackedDao.updateStatus(targets.get(idx).getId(), res.getStatus(), res.getCheckedAt());
        });

        // Đọc lại để có prev_status/last_status mới nhất rồi mới quyết định cảnh báo
        List<TrackedLink> after = trackedDao.findAll();
        int dofollow = 0;
        for (TrackedLink t : after) {
            if (CheckResult.STATUS_DOFOLLOW.equals(t.getLastStatus())) dofollow++;
        }
        List<TrackedLink> lost = LinkAlertService.findLost(after);

        boolean sent = false;
        String error = null;
        if (!lost.isEmpty() && EmailService.isConfigured()) {
            try {
                String domain = AppSettings.getTargetDomain();
                new EmailService().send(LinkAlertService.buildSubject(lost.size(), domain),
                        LinkAlertService.buildBody(lost, domain));
                sent = true;
            } catch (Exception e) {
                error = e.getMessage();
            }
        }
        return new Summary(targets.size(), dofollow, lost.size(), sent, error);
    }
}
