package com.sapota.seo.service;

import com.sapota.seo.dao.*;
import com.sapota.seo.model.*;

import java.util.*;

/** Tính các chỉ số thống kê tổng hợp từ dữ liệu trong MySQL (dùng cho tab Thống kê và báo cáo Excel). */
public class StatsService {

    public static class Snapshot {
        public int gapCount;
        public double gapAvgAs;
        public int highPriorityCount;
        public int refDomainCount;
        public long totalBacklinks;
        public double refAvgAs;
        public int checkTotal;
        public double dofollowRate;
        public int outreachTotal;
        public int outreachWon;
        public double outreachWinRate;
        public int trackedTotal;
        public int trackedLost;
        public int overdueFollowUps;
        public Map<String, Integer> checkByStatus = new LinkedHashMap<>();
        public Map<String, Integer> outreachByStatus = new LinkedHashMap<>();
        public Map<String, Integer> topCompetitors = new LinkedHashMap<>();
        public Map<String, Integer> asDistribution = new LinkedHashMap<>();
        public Map<String, Integer> backlinksByCountry = new LinkedHashMap<>();
    }

    public Snapshot compute() {
        Snapshot s = new Snapshot();

        List<GapDomain> gaps = new GapDomainDAO().findAll(0, 0);
        s.gapCount = gaps.size();
        double sumAs = 0;
        for (String k : new String[]{"<30", "30-39", "40-49", "50-59", "60+"}) s.asDistribution.put(k, 0);
        Map<String, Integer> comp = new HashMap<>();
        for (GapDomain g : gaps) {
            sumAs += g.getMaxAs();
            if (g.getPriorityScore() >= 80) s.highPriorityCount++;
            String bucket = g.getMaxAs() < 30 ? "<30" : g.getMaxAs() < 40 ? "30-39"
                    : g.getMaxAs() < 50 ? "40-49" : g.getMaxAs() < 60 ? "50-59" : "60+";
            s.asDistribution.merge(bucket, 1, Integer::sum);
            if (g.getCompetitors() != null) {
                for (String c : g.getCompetitors().split(",")) {
                    String name = c.trim().toLowerCase();
                    if (!name.isEmpty()) comp.merge(name, 1, Integer::sum);
                }
            }
        }
        s.gapAvgAs = gaps.isEmpty() ? 0 : sumAs / gaps.size();
        comp.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(8)
                .forEach(e -> s.topCompetitors.put(e.getKey(), e.getValue()));

        List<BacklinkDomain> refs = new BacklinkDAO().findAll();
        s.refDomainCount = refs.size();
        double sumRef = 0;
        Map<String, Integer> byCountry = new HashMap<>();
        for (BacklinkDomain b : refs) {
            s.totalBacklinks += b.getBacklinks();
            sumRef += b.getAscore();
            String c = b.getCountry() == null || b.getCountry().isEmpty() ? "?" : b.getCountry();
            byCountry.merge(c, 1, Integer::sum);
        }
        s.refAvgAs = refs.isEmpty() ? 0 : sumRef / refs.size();
        byCountry.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(6)
                .forEach(e -> s.backlinksByCountry.put(e.getKey(), e.getValue()));

        s.checkByStatus = new CheckResultDAO().countByStatus();
        for (int v : s.checkByStatus.values()) s.checkTotal += v;
        s.dofollowRate = s.checkTotal == 0 ? 0
                : 100.0 * s.checkByStatus.getOrDefault(CheckResult.STATUS_DOFOLLOW, 0) / s.checkTotal;

        OutreachDAO od = new OutreachDAO();
        s.outreachByStatus = od.countByStatus();
        for (int v : s.outreachByStatus.values()) s.outreachTotal += v;
        s.outreachWon = s.outreachByStatus.getOrDefault(OutreachRecord.WON, 0);
        s.outreachWinRate = s.outreachTotal == 0 ? 0 : 100.0 * s.outreachWon / s.outreachTotal;
        for (OutreachRecord r : od.findAll(null, null)) {
            if ("Quá hạn".equals(r.getReminder())) s.overdueFollowUps++;
        }

        List<TrackedLink> tracked = new TrackedLinkDAO().findAll();
        s.trackedTotal = tracked.size();
        for (TrackedLink t : tracked) if (t.isLost()) s.trackedLost++;
        return s;
    }
}
