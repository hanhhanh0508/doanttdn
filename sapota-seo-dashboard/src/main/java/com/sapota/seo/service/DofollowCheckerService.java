package com.sapota.seo.service;

import com.sapota.seo.model.CheckResult;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Service thực hiện chức năng cốt lõi của module Dofollow Checker
 * (mô tả tại mục 4.3.3 - Sơ đồ trình tự xử lý trong báo cáo):
 *
 *  1. Gửi HTTP GET request tới URL cần kiểm tra
 *  2. Nhận về nội dung HTML
 *  3. Parse HTML, tìm các thẻ <a> có href chứa domain đích (mặc định: sapotacorp.vn)
 *  4. Đọc thuộc tính rel để xác định dofollow hay nofollow
 *  5. Trả về kết quả dưới dạng CheckResult
 */
public class DofollowCheckerService {

    private static final String DEFAULT_TARGET_DOMAIN = "sapotacorp.vn";
    private static final int TIMEOUT_MS = 8000;
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) SapotaSEODashboard/1.0";

    private final String targetDomain;

    public DofollowCheckerService() {
        this(DEFAULT_TARGET_DOMAIN);
    }

    public DofollowCheckerService(String targetDomain) {
        this.targetDomain = targetDomain;
    }

    /**
     * Kiểm tra 1 URL: tải HTML, tìm liên kết trỏ về targetDomain,
     * và xác định trạng thái dofollow/nofollow.
     */
    public CheckResult check(String url) {
        url = url.trim();
        if (url.isEmpty()) {
            return new CheckResult(url, CheckResult.STATUS_ERROR, "URL rỗng");
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .get();

            Elements links = doc.select("a[href*=" + targetDomain + "]");

            if (links.isEmpty()) {
                return new CheckResult(url, CheckResult.STATUS_NOT_FOUND,
                        "Không tìm thấy liên kết trỏ về " + targetDomain + " trên trang này");
            }

            // Nếu có nhiều liên kết, chỉ cần 1 liên kết dofollow là coi như trang này dofollow
            for (Element link : links) {
                String rel = link.attr("rel").toLowerCase();
                boolean isNofollow = rel.contains("nofollow");
                if (!isNofollow) {
                    return new CheckResult(url, CheckResult.STATUS_DOFOLLOW,
                            "Tìm thấy " + links.size() + " liên kết, có ít nhất 1 liên kết dofollow");
                }
            }

            return new CheckResult(url, CheckResult.STATUS_NOFOLLOW,
                    "Tìm thấy " + links.size() + " liên kết, tất cả đều gắn rel=nofollow");

        } catch (IOException e) {
            return new CheckResult(url, CheckResult.STATUS_ERROR,
                    "Không truy cập được trang (timeout/lỗi mạng/trang chặn bot): " + e.getMessage());
        } catch (Exception e) {
            return new CheckResult(url, CheckResult.STATUS_ERROR, "Lỗi không xác định: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra xem domain có đang bị chuyển hướng (redirect) sang domain khác
     * hay không — dùng cho trường hợp công ty đổi tên/đổi domain và có đặt
     * redirect 301 từ domain cũ sang domain mới. Đây là việc gọi thẳng tới
     * domain đó nên tự động hoá được, khác với việc tìm tên công ty mới trên
     * Google (không tự động hoá được vì lý do ToS).
     */
    public RedirectResult checkRedirect(String domain) {
        String originalUrl = normalizeDomain(domain);
        try {
            Connection.Response res = Jsoup.connect(originalUrl)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true)
                    .followRedirects(true)
                    .execute();

            String finalUrl = res.url().toString();
            String originalHost = hostOf(originalUrl);
            String finalHost = hostOf(finalUrl);

            boolean redirected = !originalHost.equalsIgnoreCase(finalHost);
            return new RedirectResult(originalUrl, finalUrl, redirected, res.statusCode(), null);

        } catch (Exception e) {
            return new RedirectResult(originalUrl, null, false, -1, e.getMessage());
        }
    }

    private String normalizeDomain(String domain) {
        String d = domain.trim();
        if (!d.startsWith("http://") && !d.startsWith("https://")) {
            d = "https://" + d;
        }
        return d;
    }

    private String hostOf(String url) {
        try {
            String host = java.net.URI.create(url).getHost();
            return host == null ? url : host;
        } catch (Exception e) {
            return url;
        }
    }

    /** Kết quả kiểm tra redirect: domain gốc, domain cuối cùng, có bị đổi hay không. */
    public static class RedirectResult {
        public final String originalUrl;
        public final String finalUrl;
        public final boolean redirected;
        public final int statusCode;
        public final String error;

        public RedirectResult(String originalUrl, String finalUrl, boolean redirected, int statusCode, String error) {
            this.originalUrl = originalUrl;
            this.finalUrl = finalUrl;
            this.redirected = redirected;
            this.statusCode = statusCode;
            this.error = error;
        }
    }
}
