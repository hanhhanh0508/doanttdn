package com.sapota.seo.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Lưu các cài đặt của ứng dụng (trọng số điểm ưu tiên, domain đích mặc định)
 * vào file ~/.sapota-seo-dashboard/settings.properties để giữ lại giữa các lần mở app.
 */
public final class AppSettings {

    private static final Path DIR = Path.of(System.getProperty("user.home"), ".sapota-seo-dashboard");
    private static final Path FILE = DIR.resolve("settings.properties");
    private static final Properties PROPS = new Properties();

    static {
        if (Files.exists(FILE)) {
            try (InputStream in = Files.newInputStream(FILE)) {
                PROPS.load(in);
            } catch (IOException ignored) {
                // dùng giá trị mặc định
            }
        }
    }

    private AppSettings() {
    }

    public static synchronized double getWeightAs() {
        return getDouble("weight.as", 1.0);
    }

    public static synchronized double getWeightCompetitor() {
        return getDouble("weight.competitor", 5.0);
    }

    public static synchronized String getTargetDomain() {
        String d = PROPS.getProperty("target.domain", "sapotacorp.vn").trim();
        return d.isEmpty() ? "sapotacorp.vn" : d;
    }

    public static synchronized void save(double weightAs, double weightCompetitor, String targetDomain) throws IOException {
        PROPS.setProperty("weight.as", String.valueOf(weightAs));
        PROPS.setProperty("weight.competitor", String.valueOf(weightCompetitor));
        PROPS.setProperty("target.domain", targetDomain == null ? "sapotacorp.vn" : targetDomain.trim());
        Files.createDirectories(DIR);
        try (OutputStream out = Files.newOutputStream(FILE)) {
            PROPS.store(out, "SapotaCorp SEO Dashboard settings");
        }
    }

    // ---- Cấu hình kiểm tra tự động & email cảnh báo ----

    public static synchronized boolean isScheduleEnabled() {
        return Boolean.parseBoolean(PROPS.getProperty("schedule.enabled", "false"));
    }

    /** Giờ chạy kiểm tra hằng ngày, dạng HH:mm (mặc định 08:00). */
    public static synchronized String getScheduleTime() {
        return PROPS.getProperty("schedule.time", "08:00").trim();
    }

    public static synchronized boolean isEmailEnabled() {
        return Boolean.parseBoolean(PROPS.getProperty("email.enabled", "false"));
    }

    public static synchronized String getSmtpHost() {
        return PROPS.getProperty("smtp.host", "smtp.gmail.com").trim();
    }

    public static synchronized int getSmtpPort() {
        try {
            return Integer.parseInt(PROPS.getProperty("smtp.port", "587").trim());
        } catch (NumberFormatException e) {
            return 587;
        }
    }

    public static synchronized String getSmtpUser() {
        return PROPS.getProperty("smtp.user", "").trim();
    }

    public static synchronized String getSmtpPassword() {
        return PROPS.getProperty("smtp.password", "");
    }

    public static synchronized String getEmailTo() {
        return PROPS.getProperty("email.to", "").trim();
    }

    public static synchronized void saveSchedule(boolean scheduleEnabled, String time, boolean emailEnabled,
                                                 String host, int port, String user, String password,
                                                 String emailTo) throws IOException {
        PROPS.setProperty("schedule.enabled", String.valueOf(scheduleEnabled));
        PROPS.setProperty("schedule.time", time == null ? "08:00" : time.trim());
        PROPS.setProperty("email.enabled", String.valueOf(emailEnabled));
        PROPS.setProperty("smtp.host", host == null ? "" : host.trim());
        PROPS.setProperty("smtp.port", String.valueOf(port));
        PROPS.setProperty("smtp.user", user == null ? "" : user.trim());
        PROPS.setProperty("smtp.password", password == null ? "" : password);
        PROPS.setProperty("email.to", emailTo == null ? "" : emailTo.trim());
        Files.createDirectories(DIR);
        try (OutputStream out = Files.newOutputStream(FILE)) {
            PROPS.store(out, "SapotaCorp SEO Dashboard settings");
        }
    }

    private static double getDouble(String key, double def) {
        try {
            return Double.parseDouble(PROPS.getProperty(key, String.valueOf(def)));
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
