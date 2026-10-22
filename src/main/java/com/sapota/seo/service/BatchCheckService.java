package com.sapota.seo.service;

import com.sapota.seo.model.CheckResult;

import java.util.List;
import java.util.concurrent.*;

/** Kiểm tra dofollow nhiều URL song song bằng thread pool (nhanh hơn kiểm tra tuần tự). */
public final class BatchCheckService {

    public interface Listener {
        /** Gọi trên luồng gọi checkAll mỗi khi có 1 URL kiểm tra xong. */
        void onResult(int index, CheckResult result, int done, int total);
    }

    private BatchCheckService() {
    }

    public static void checkAll(List<String> urls, String targetDomain, int threads, Listener listener) {
        DofollowCheckerService service = new DofollowCheckerService(targetDomain);
        ExecutorService pool = Executors.newFixedThreadPool(Math.max(1, threads));
        CompletionService<Object[]> cs = new ExecutorCompletionService<>(pool);
        try {
            for (int i = 0; i < urls.size(); i++) {
                final int idx = i;
                cs.submit(() -> new Object[]{idx, service.check(urls.get(idx))});
            }
            for (int done = 1; done <= urls.size(); done++) {
                try {
                    Object[] res = cs.take().get();
                    listener.onResult((Integer) res[0], (CheckResult) res[1], done, urls.size());
                } catch (ExecutionException e) {
                    System.err.println("Lỗi khi kiểm tra: " + e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } finally {
            pool.shutdownNow();
        }
    }
}
