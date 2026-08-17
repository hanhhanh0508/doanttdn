package com.sapota.seo.service;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.List;

/**
 * Service tự động dò tìm trang "đăng ký doanh nghiệp" (submit/register/join)
 * trên một domain — bước "tìm trang đăng ký trong enterpriseleague.com"
 * mà sếp yêu cầu.
 *
 * Cách làm: thử lần lượt các đường dẫn phổ biến mà các site directory/B2B
 * hay dùng, gửi HTTP GET tới từng đường dẫn, giữ lại những đường dẫn trả về
 * mã 200 (tồn tại thật) thay vì 404.
 *
 * Lưu ý: đây là việc gọi trực tiếp tới chính website đích (không thông qua
 * Google), nên hoàn toàn khác với việc cào kết quả tìm kiếm của Google.
 */
public class RegistrationPageFinderService {

    private static final int TIMEOUT_MS = 6000;
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) SapotaSEODashboard/1.0";

    /** Danh sách các đường dẫn thường gặp cho trang đăng ký/tham gia/gửi doanh nghiệp. */
    private static final String[] CANDIDATE_PATHS = {
            "/register", "/registration", "/sign-up", "/signup",
            "/join", "/join-us", "/submit", "/submit-business",
            "/add-business", "/add-company", "/add-listing",
            "/list-your-business", "/list-business", "/get-listed",
            "/contribute", "/advertise", "/directory/submit",
            "/business-directory/add"
    };

    public static class FoundPage {
        public final String url;
        public final int statusCode;

        public FoundPage(String url, int statusCode) {
            this.url = url;
            this.statusCode = statusCode;
        }
    }

    /**
     * Dò tìm trang đăng ký trên domain đã cho.
     *
     * @param domain domain cần dò, ví dụ "enterpriseleague.com" (có hoặc không có https://)
     * @return danh sách các URL tồn tại thật (HTTP 200) trong số các đường dẫn phổ biến
     */
    public List<FoundPage> find(String domain) {
        String base = normalizeDomain(domain);
        List<FoundPage> found = new ArrayList<>();

        for (String path : CANDIDATE_PATHS) {
            String url = base + path;
            try {
                Connection.Response res = Jsoup.connect(url)
                        .userAgent(USER_AGENT)
                        .timeout(TIMEOUT_MS)
                        .ignoreHttpErrors(true)
                        .ignoreContentType(true)
                        .followRedirects(true)
                        .execute();

                if (res.statusCode() == 200) {
                    found.add(new FoundPage(url, res.statusCode()));
                }
            } catch (Exception e) {
                // Bỏ qua đường dẫn lỗi (timeout, không tồn tại...) và thử đường dẫn kế tiếp
            }
        }
        return found;
    }

    private String normalizeDomain(String domain) {
        String d = domain.trim();
        if (!d.startsWith("http://") && !d.startsWith("https://")) {
            d = "https://" + d;
        }
        if (d.endsWith("/")) {
            d = d.substring(0, d.length() - 1);
        }
        return d;
    }
}
