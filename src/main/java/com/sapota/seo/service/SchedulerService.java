package com.sapota.seo.service;

import com.sapota.seo.util.AppSettings;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Lên lịch kiểm tra backlink tự động mỗi ngày vào giờ cấu hình (mặc định 08:00),
 * dùng ScheduledExecutorService của JDK trên 1 luồng nền (daemon, không chặn việc tắt app).
 * Lưu ý: app phải đang mở thì lịch mới chạy (đây là ứng dụng desktop, không phải dịch vụ chạy nền).
 */
public final class SchedulerService {

    private static final SchedulerService INSTANCE = new SchedulerService();
    private static final long DAY_SECONDS = Duration.ofDays(1).toSeconds();

    private ScheduledExecutorService executor;
    private volatile String lastRunMessage = "Chưa chạy lần nào";
    private volatile Consumer<String> onRun;

    private SchedulerService() {
    }

    public static SchedulerService getInstance() {
        return INSTANCE;
    }

    /** Số giây từ "now" tới lần chạy kế tiếp lúc "target" (hôm nay nếu chưa qua giờ, nếu không thì ngày mai). */
    public static long secondsUntilNextRun(LocalDateTime now, LocalTime target) {
        LocalDateTime next = now.toLocalDate().atTime(target);
        if (!next.isAfter(now)) next = next.plusDays(1);
        return Duration.between(now, next).getSeconds();
    }

    /** Đọc giờ dạng HH:mm; sai định dạng thì trả null. */
    public static LocalTime parseTime(String text) {
        try {
            return LocalTime.parse(text.trim());
        } catch (DateTimeParseException | NullPointerException e) {
            return null;
        }
    }

    /** Bật lịch theo cấu hình hiện tại; gọi lại sẽ huỷ lịch cũ và đặt lịch mới. */
    public synchronized void restartFromSettings() {
        stop();
        if (!AppSettings.isScheduleEnabled()) return;
        LocalTime time = parseTime(AppSettings.getScheduleTime());
        if (time == null) return;

        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "backlink-scheduler");
            t.setDaemon(true);
            return t;
        });
        long delay = secondsUntilNextRun(LocalDateTime.now(), time);
        executor.scheduleAtFixedRate(this::runNow, delay, DAY_SECONDS, TimeUnit.SECONDS);
    }

    public synchronized void stop() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    public synchronized boolean isRunning() {
        return executor != null;
    }

    /** Chạy ngay 1 lượt (dùng cho lịch tự động và nút "Chạy kiểm tra ngay"). Không ném lỗi ra ngoài. */
    public String runNow() {
        String msg;
        try {
            msg = new MonitorRunner().runOnce().toString();
        } catch (Exception e) {
            msg = "Lỗi khi chạy kiểm tra: " + e.getMessage();
        }
        lastRunMessage = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                + " - " + msg;
        Consumer<String> cb = onRun;
        if (cb != null) cb.accept(lastRunMessage);
        return lastRunMessage;
    }

    public String getLastRunMessage() {
        return lastRunMessage;
    }

    /** Đăng ký callback để giao diện cập nhật trạng thái sau mỗi lượt chạy (callback chạy trên luồng nền). */
    public void setOnRun(Consumer<String> callback) {
        this.onRun = callback;
    }
}
