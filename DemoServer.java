import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Web demo chạy trên máy em để thử Dofollow Checker / Backlink Monitor mà không cần trang thật.
 *
 * Chạy:   java DemoServer.java          (mặc định cổng 8000; đổi cổng: java DemoServer.java 9000)
 * Mở:     http://localhost:8000/        (bảng điều khiển: bấm để đổi link của từng bài)
 * Bài:    http://localhost:8000/bai-viet-1  (và /bai-viet-2, /bai-viet-3)
 *
 * Mỗi bài có 1 link trỏ về sapotacorp.vn ở 1 trong 3 chế độ: dofollow, nofollow, hoặc đã gỡ link.
 */
public class DemoServer {

    private static final Map<String, String> MODE = new ConcurrentHashMap<>();
    private static final Map<String, String> LABEL = Map.of(
            "dofollow", "Dofollow (link bình thường)",
            "nofollow", "Nofollow (rel=\"nofollow\")",
            "removed", "Đã gỡ link");

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8000;
        MODE.put("1", "dofollow");
        MODE.put("2", "nofollow");
        MODE.put("3", "removed");

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", DemoServer::handle);
        server.start();
        System.out.println("Web demo dang chay. Mo trinh duyet: http://localhost:" + port + "/");
        System.out.println("Nhan Ctrl+C de dung.");
    }

    private static void handle(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (path.equals("/")) {
            send(ex, 200, index());
        } else if (path.matches("/bai-viet-[123]")) {
            send(ex, 200, article(path.substring(path.length() - 1)));
        } else if (path.equals("/doi")) {
            Map<String, String> q = query(ex.getRequestURI().getRawQuery());
            String trang = q.get("trang");
            String cheDo = q.get("che-do");
            if (MODE.containsKey(trang) && LABEL.containsKey(cheDo)) MODE.put(trang, cheDo);
            ex.getResponseHeaders().add("Location", "/");
            ex.sendResponseHeaders(302, -1);
            ex.close();
        } else {
            send(ex, 404, page("Khong tim thay", "<p>Trang khong ton tai. <a href=\"/\">Ve bang dieu khien</a></p>"));
        }
    }

    private static String index() {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Web demo: bảng điều khiển</h1>")
          .append("<p>Dán 3 URL bên dưới vào <b>Dofollow Checker</b> hoặc <b>Backlink Monitor</b>. ")
          .append("Bấm các nút để đổi trạng thái link, rồi quay lại app bấm kiểm tra.</p><table border=\"1\" cellpadding=\"8\" cellspacing=\"0\">")
          .append("<tr><th>URL</th><th>Trạng thái link hiện tại</th><th>Đổi sang</th></tr>");
        for (String id : new String[]{"1", "2", "3"}) {
            String m = MODE.get(id);
            sb.append("<tr><td><code>http://localhost:").append("8000").append("/bai-viet-").append(id).append("</code></td>")
              .append("<td><b>").append(LABEL.get(m)).append("</b></td><td>");
            for (String target : new String[]{"dofollow", "nofollow", "removed"}) {
                if (!target.equals(m)) {
                    sb.append("<a href=\"/doi?trang=").append(id).append("&che-do=").append(target).append("\">")
                      .append(target.equals("dofollow") ? "Dofollow" : target.equals("nofollow") ? "Nofollow" : "Gỡ link")
                      .append("</a> &nbsp; ");
                }
            }
            sb.append("</td></tr>");
        }
                return page("Web demo backlink", sb.toString());
    }

    private static String article(String id) {
        String m = MODE.get(id);
        String link;
        switch (m) {
            case "dofollow":
                link = "<a href=\"https://sapotacorp.vn/\">SapotaCorp - công ty phần mềm</a>";
                break;
            case "nofollow":
                link = "<a href=\"https://sapotacorp.vn/\" rel=\"nofollow\">SapotaCorp - công ty phần mềm</a>";
                break;
            default:
                link = "SapotaCorp - công ty phần mềm (đã bị gỡ link)";
        }
        String body = "<h1>Bài viết khách số " + id + "</h1>"
                + "<p>Đây là bài viết thử nghiệm cho đồ án. Doanh nghiệp được giới thiệu: " + link + ".</p>"
                + "<p>Nội dung bài viết giả lập, dùng để kiểm tra khả năng nhận diện backlink của ứng dụng.</p>";
        return page("Bài viết khách " + id, body);
    }

    private static String page(String title, String body) {
        return "<!DOCTYPE html><html lang=\"vi\"><head><meta charset=\"utf-8\"><title>" + title + "</title></head>"
                + "<body style=\"font-family:sans-serif;max-width:760px;margin:30px auto\">" + body + "</body></html>";
    }

    private static Map<String, String> query(String raw) {
        Map<String, String> map = new HashMap<>();
        if (raw == null) return map;
        for (String pair : raw.split("&")) {
            int i = pair.indexOf('=');
            if (i > 0) map.put(URLDecoder.decode(pair.substring(0, i), StandardCharsets.UTF_8),
                    URLDecoder.decode(pair.substring(i + 1), StandardCharsets.UTF_8));
        }
        return map;
    }

    private static void send(HttpExchange ex, int code, String html) throws IOException {
        byte[] b = html.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        ex.sendResponseHeaders(code, b.length);
        try (OutputStream o = ex.getResponseBody()) {
            o.write(b);
        }
    }
}
