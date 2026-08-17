package com.sapota.seo.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Lưu Gemini API Key vào 1 file cấu hình cục bộ trong thư mục home của
 * người dùng, để không phải nhập lại API key mỗi lần mở ứng dụng.
 *
 * File lưu tại: ~/.sapota-seo-dashboard/gemini.properties
 * (không commit file này lên Git — xem .gitignore)
 */
public final class ApiKeyStore {

    private static final Path CONFIG_DIR =
            Path.of(System.getProperty("user.home"), ".sapota-seo-dashboard");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("gemini.properties");
    private static final String KEY_NAME = "gemini.api.key";

    private ApiKeyStore() {
    }

    public static void save(String apiKey) {
        try {
            Files.createDirectories(CONFIG_DIR);
            Properties props = new Properties();
            props.setProperty(KEY_NAME, apiKey == null ? "" : apiKey);
            try (FileOutputStream out = new FileOutputStream(CONFIG_FILE.toFile())) {
                props.store(out, "Sapota SEO Dashboard - Gemini API Key (giữ bí mật, không chia sẻ file này)");
            }
        } catch (IOException e) {
            System.err.println("Không lưu được API key: " + e.getMessage());
        }
    }

    public static String load() {
        if (!Files.exists(CONFIG_FILE)) {
            return "";
        }
        try (FileInputStream in = new FileInputStream(CONFIG_FILE.toFile())) {
            Properties props = new Properties();
            props.load(in);
            return props.getProperty(KEY_NAME, "");
        } catch (IOException e) {
            return "";
        }
    }
}
