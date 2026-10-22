package com.sapota.seo.service;

import com.sapota.seo.dao.*;
import com.sapota.seo.model.*;
import com.sapota.seo.ui.UiUtil;
import com.sapota.seo.util.ExcelExporter;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Xuất báo cáo tổng hợp toàn bộ dữ liệu ra 1 file Excel nhiều sheet. */
public class ReportService {

    public void exportFullReport(String filePath) throws IOException {
        StatsService.Snapshot s = new StatsService().compute();
        try (Workbook wb = new XSSFWorkbook()) {

            List<Object[]> overview = new ArrayList<>();
            overview.add(new Object[]{"Ngày xuất báo cáo", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))});
            overview.add(new Object[]{"Số domain backlink gap", s.gapCount});
            overview.add(new Object[]{"AS tối đa trung bình (gap)", Math.round(s.gapAvgAs * 10) / 10.0});
            overview.add(new Object[]{"Domain ưu tiên cao (điểm >= 80)", s.highPriorityCount});
            overview.add(new Object[]{"Số domain đang trỏ backlink về", s.refDomainCount});
            overview.add(new Object[]{"Tổng số backlink hiện có", s.totalBacklinks});
            overview.add(new Object[]{"Tổng lượt kiểm tra dofollow", s.checkTotal});
            overview.add(new Object[]{"Tỷ lệ Dofollow (%)", Math.round(s.dofollowRate * 10) / 10.0});
            overview.add(new Object[]{"Số domain đang outreach", s.outreachTotal});
            overview.add(new Object[]{"Số domain đã đặt được link", s.outreachWon});
            overview.add(new Object[]{"Tỷ lệ thành công outreach (%)", Math.round(s.outreachWinRate * 10) / 10.0});
            overview.add(new Object[]{"Outreach quá hạn follow-up", s.overdueFollowUps});
            overview.add(new Object[]{"Backlink đang theo dõi", s.trackedTotal});
            overview.add(new Object[]{"Backlink bị mất/đổi trạng thái", s.trackedLost});
            ExcelExporter.writeSheet(wb, "Tổng quan", Arrays.asList("Chỉ số", "Giá trị"), overview);

            List<Object[]> gap = new ArrayList<>();
            for (GapDomain g : new GapDomainDAO().findAll(0, 0)) {
                gap.add(new Object[]{g.getDomain(), g.getMaxAs(), g.getCompetitorCount(),
                        g.getPriorityScore(), g.getPriorityLevel(), g.getCompetitors()});
            }
            ExcelExporter.writeSheet(wb, "Backlink Gap", Arrays.asList(
                    "Domain", "MaxAS", "Số đối thủ", "Điểm ưu tiên", "Mức ưu tiên", "Đối thủ sở hữu"), gap);

            List<Object[]> refs = new ArrayList<>();
            for (BacklinkDomain b : new BacklinkDAO().findAll()) {
                refs.add(new Object[]{b.getDomain(), b.getAscore(), b.getBacklinks(), b.getCountry()});
            }
            ExcelExporter.writeSheet(wb, "Backlink hiện có", Arrays.asList("Domain", "AS", "Backlinks", "Quốc gia"), refs);

            List<Object[]> out = new ArrayList<>();
            for (OutreachRecord r : new OutreachDAO().findAll(null, null)) {
                out.add(new Object[]{r.getDomain(), r.getStatus(), r.getContact(),
                        UiUtil.formatDate(r.getLastContact()), UiUtil.formatDate(r.getFollowUp()), r.getNote()});
            }
            ExcelExporter.writeSheet(wb, "Outreach", Arrays.asList(
                    "Domain", "Trạng thái", "Liên hệ", "Liên hệ gần nhất", "Hẹn follow-up", "Ghi chú"), out);

            List<Object[]> tr = new ArrayList<>();
            for (TrackedLink t : new TrackedLinkDAO().findAll()) {
                tr.add(new Object[]{t.getUrl(), t.getPlatform(), UiUtil.formatDate(t.getPlacedDate()),
                        t.getLastStatus(), t.getPrevStatus(), t.isLost() ? "Mất/đổi link" : "", t.getNote()});
            }
            ExcelExporter.writeSheet(wb, "Backlink Monitor", Arrays.asList(
                    "URL", "Nền tảng", "Ngày đặt", "Trạng thái", "Trạng thái trước", "Cảnh báo", "Ghi chú"), tr);

            List<Object[]> hist = new ArrayList<>();
            for (CheckResult c : new CheckResultDAO().findFiltered(null, null, 5000)) {
                hist.add(new Object[]{c.getUrl(), c.getStatus(), c.getCheckedAtFormatted(), c.getNote()});
            }
            ExcelExporter.writeSheet(wb, "Lịch sử kiểm tra", Arrays.asList("URL", "Trạng thái", "Thời gian", "Ghi chú"), hist);

            ExcelExporter.save(wb, filePath);
        }
    }
}
