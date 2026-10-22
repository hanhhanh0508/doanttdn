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

    private static double getDouble(String key, double def) {
        try {
            return Double.parseDouble(PROPS.getProperty(key, String.valueOf(def)));
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
