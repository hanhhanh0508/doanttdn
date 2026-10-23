package com.sapota.seo.service;

import com.sapota.seo.model.CheckResult;
import com.sapota.seo.model.TrackedLink;

import java.util.ArrayList;
import java.util.List;

/**
 * Logic thuần (không dùng mạng/CSDL) để quyết định link nào cần cảnh báo và soạn nội dung email.
 * Tách riêng để unit test được.
 */
public final class LinkAlertService {

    private LinkAlertService() {
    }

    /**
     * Link cần cảnh báo = từng Dofollow ở lần kiểm tra trước, lần này không còn Dofollow.
     * Bỏ qua trạng thái "Lỗi truy cập" vì thường chỉ là timeout/chặn bot tạm thời, tránh báo động giả.
     */
    public static List<TrackedLink> findLost(List<TrackedLink> links) {
        List<TrackedLink> lost = new ArrayList<>();
        for (TrackedLink t : links) {
            if (t.isLost() && !CheckResult.STATUS_ERROR.equals(t.getLastStatus())) {
                lost.add(t);
            }
        }
        return lost;
    }

    public static String buildSubject(int lostCount, String targetDomain) {
        return "[SEO Dashboard] Cảnh báo: " + lostCount + " backlink về " + targetDomain + " bị mất/đổi";
    }

    public static String buildBody(List<TrackedLink> lost, String targetDomain) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hệ thống phát hiện ").append(lost.size())
                .append(" backlink trỏ về ").append(targetDomain)
                .append(" không còn ở trạng thái Dofollow:\n\n");
        int i = 1;
        for (TrackedLink t : lost) {
            sb.append(i++).append(". ").append(t.getUrl()).append('\n');
            sb.append("   Trước: ").append(t.getPrevStatus())
                    .append("  ->  Hiện tại: ").append(t.getLastStatus()).append('\n');
            if (t.getPlatform() != null && !t.getPlatform().isBlank()) {
                sb.append("   Nền tảng: ").append(t.getPlatform()).append('\n');
            }
            sb.append('\n');
        }
        sb.append("Vui lòng kiểm tra lại và liên hệ chủ website nếu cần khôi phục link.\n");
        sb.append("-- Email tự động từ SapotaCorp SEO Dashboard");
        return sb.toString();
    }
}
