package com.sapota.seo.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Service tạo nội dung bài đăng LinkedIn dựa trên 1 đường link bài viết,
 * dùng Gemini API (Google AI Studio).
 *
 * Quy trình:
 *  1. Tải nội dung trang web từ URL (dùng Jsoup) — lấy tiêu đề + nội dung chính.
 *  2. Ghép nội dung đó vào 1 prompt yêu cầu Gemini viết thành bài LinkedIn
 *     tiếng Việt, văn phong chuyên nghiệp (giống các bài công ty đã đăng).
 *  3. Gọi Gemini API (generateContent), lấy văn bản trả về.
 *
 * Cần có Gemini API Key miễn phí tại https://aistudio.google.com/apikey
 */
public class GeminiPostGeneratorService {

    // "gemini-flash-latest" là alias luôn tự trỏ tới bản Flash mới nhất của Google,
    // tránh việc phải sửa code mỗi khi Google ngừng hỗ trợ 1 phiên bản cụ thể
    // (như gemini-2.0-flash đã bị ngừng hỗ trợ).
    private static final String MODEL = "gemini-flash-latest";
    private static final String API_URL_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    private static final int MAX_CONTENT_CHARS = 6000; // giới hạn độ dài nội dung đưa vào prompt
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) SapotaSEODashboard/1.0";

    /**
     * Đọc nội dung từ URL, gửi cho Gemini, trả về nội dung bài post đã soạn.
     *
     * @param url             link bài viết nguồn
     * @param apiKey          Gemini API Key
     * @param extraInstruction yêu cầu thêm của người dùng (có thể để trống),
     *                         ví dụ "viết ngắn gọn hơn", "nhấn mạnh về AI Agent"...
     */
    public String generatePost(String url, String apiKey, String extraInstruction) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("Chưa nhập Gemini API Key.");
        }

        ArticleContent article = fetchArticle(url);
        String prompt = buildPrompt(article, extraInstruction);
        return callGemini(prompt, apiKey);
    }

    // ---------------------------------------------------------------------
    // Bước 1: Tải nội dung bài viết từ URL
    // ---------------------------------------------------------------------
    private ArticleContent fetchArticle(String url) throws Exception {
        String u = url.trim();
        if (!u.startsWith("http://") && !u.startsWith("https://")) {
            u = "https://" + u;
        }

        Document doc = Jsoup.connect(u)
                .userAgent(USER_AGENT)
                .timeout(10000)
                .followRedirects(true)
                .get();

        String title = doc.title();
        String bodyText = doc.body() != null ? doc.body().text() : "";
        if (bodyText.length() > MAX_CONTENT_CHARS) {
            bodyText = bodyText.substring(0, MAX_CONTENT_CHARS);
        }
        return new ArticleContent(u, title, bodyText);
    }

    // ---------------------------------------------------------------------
    // Bước 2: Soạn prompt gửi cho Gemini
    // ---------------------------------------------------------------------
    private String buildPrompt(ArticleContent article, String extraInstruction) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là chuyên viên Content/SEO của công ty SapotaCorp — công ty cung cấp kỹ sư phần mềm ")
          .append("(Salesforce, Power Platform, AI Agent, Custom Software) cho khách hàng quốc tế.\n\n");
        sb.append("Hãy viết 1 bài đăng LinkedIn bằng tiếng Việt dựa trên bài viết nguồn dưới đây, ")
          .append("theo phong cách chuyên nghiệp, súc tích, dễ đọc, phù hợp đối tượng là khách hàng doanh nghiệp B2B.\n\n");
        sb.append("Yêu cầu:\n");
        sb.append("- Độ dài khoảng 120-200 từ.\n");
        sb.append("- Mở đầu bằng 1 câu hook thu hút.\n");
        sb.append("- Không dùng markdown (không dùng dấu *, #).\n");
        sb.append("- Kết thúc bằng 1 câu kêu gọi hành động (đọc thêm / liên hệ / thảo luận).\n");
        sb.append("- Thêm 3-5 hashtag liên quan ở cuối bài.\n");
        if (extraInstruction != null && !extraInstruction.isBlank()) {
            sb.append("- Yêu cầu thêm từ người dùng: ").append(extraInstruction.trim()).append("\n");
        }
        sb.append("\n--- Bài viết nguồn ---\n");
        sb.append("Tiêu đề: ").append(article.title).append("\n");
        sb.append("Link: ").append(article.url).append("\n");
        sb.append("Nội dung:\n").append(article.bodyText).append("\n");
        sb.append("--- Hết bài viết nguồn ---\n\n");
        sb.append("Chỉ trả về nội dung bài đăng LinkedIn, không giải thích thêm gì khác.");
        return sb.toString();
    }

    // ---------------------------------------------------------------------
    // Bước 3: Gọi Gemini API
    // ---------------------------------------------------------------------
    private String callGemini(String prompt, String apiKey) throws Exception {
        JSONObject part = new JSONObject().put("text", prompt);
        JSONObject content = new JSONObject().put("parts", new JSONArray().put(part));
        JSONObject body = new JSONObject().put("contents", new JSONArray().put(content));

        String url = String.format(API_URL_TEMPLATE, MODEL);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("x-goog-api-key", apiKey)
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini API trả về lỗi (HTTP " + response.statusCode() + "): "
                    + shorten(response.body(), 500));
        }

        JSONObject json = new JSONObject(response.body());
        JSONArray candidates = json.optJSONArray("candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new RuntimeException("Gemini không trả về nội dung. Phản hồi: " + shorten(response.body(), 500));
        }

        JSONObject firstCandidate = candidates.getJSONObject(0);
        JSONArray parts = firstCandidate.getJSONObject("content").getJSONArray("parts");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < parts.length(); i++) {
            result.append(parts.getJSONObject(i).optString("text", ""));
        }
        return result.toString().trim();
    }

    private String shorten(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "..." : s;
    }

    private static class ArticleContent {
        final String url;
        final String title;
        final String bodyText;

        ArticleContent(String url, String title, String bodyText) {
            this.url = url;
            this.title = title;
            this.bodyText = bodyText;
        }
    }
}
