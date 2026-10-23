package com.sapota.seo.service;

import com.sapota.seo.model.CheckResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Kiểm thử cơ chế kiểm tra song song bằng hàm kiểm tra giả (không gọi mạng thật). */
class BatchCheckServiceTest {

    private List<String> urls(int n) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < n; i++) list.add("https://site" + i + ".com");
        return list;
    }

    @Test
    void everyUrlIsCheckedExactlyOnceAndResultsMatchTheirIndex() {
        List<String> urls = urls(25);
        Set<Integer> seen = Collections.synchronizedSet(new HashSet<>());
        List<String> mismatches = Collections.synchronizedList(new ArrayList<>());

        BatchCheckService.checkAll(urls,
                u -> new CheckResult(u, CheckResult.STATUS_DOFOLLOW, "ok"),
                5,
                (idx, res, done, total) -> {
                    seen.add(idx);
                    if (!urls.get(idx).equals(res.getUrl())) mismatches.add(res.getUrl());
                });

        assertEquals(25, seen.size());
        assertTrue(mismatches.isEmpty(), "kết quả phải khớp đúng URL theo chỉ số");
    }

    @Test
    void progressCounterReachesTotal() {
        List<String> urls = urls(10);
        List<Integer> doneValues = Collections.synchronizedList(new ArrayList<>());

        BatchCheckService.checkAll(urls,
                u -> new CheckResult(u, CheckResult.STATUS_NOFOLLOW, ""),
                3,
                (idx, res, done, total) -> doneValues.add(done));

        assertEquals(10, doneValues.size());
        assertEquals(10, Collections.max(doneValues));
    }

    @Test
    void usesMoreThanOneThread() {
        Set<String> threadNames = ConcurrentHashMap.newKeySet();
        BatchCheckService.checkAll(urls(20),
                u -> {
                    threadNames.add(Thread.currentThread().getName());
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return new CheckResult(u, CheckResult.STATUS_DOFOLLOW, "");
                },
                5,
                (idx, res, done, total) -> { });
        assertTrue(threadNames.size() > 1, "phải chạy trên nhiều luồng, thực tế: " + threadNames);
    }

    @Test
    void oneFailingCheckDoesNotStopTheOthers() {
        List<String> urls = urls(6);
        List<Integer> okIdx = Collections.synchronizedList(new ArrayList<>());

        BatchCheckService.checkAll(urls,
                u -> {
                    if (u.equals(urls.get(2))) throw new IllegalStateException("giả lập lỗi");
                    return new CheckResult(u, CheckResult.STATUS_DOFOLLOW, "");
                },
                2,
                (idx, res, done, total) -> okIdx.add(idx));

        assertEquals(5, okIdx.size());
        assertFalse(okIdx.contains(2));
    }

    @Test
    void emptyListDoesNothing() {
        List<Integer> calls = new ArrayList<>();
        BatchCheckService.checkAll(new ArrayList<>(),
                u -> new CheckResult(u, CheckResult.STATUS_DOFOLLOW, ""),
                5,
                (idx, res, done, total) -> calls.add(idx));
        assertTrue(calls.isEmpty());
    }
}
