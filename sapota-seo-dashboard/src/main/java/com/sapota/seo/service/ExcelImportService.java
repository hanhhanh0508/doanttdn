package com.sapota.seo.service;

import com.sapota.seo.dao.BacklinkDAO;
import com.sapota.seo.dao.GapDomainDAO;
import com.sapota.seo.model.BacklinkDomain;
import com.sapota.seo.model.GapDomain;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Service đọc dữ liệu từ 2 file Excel đầu vào (mô tả tại mục 4.3.4 báo cáo)
 * và nạp vào MySQL, thay cho việc xử lý bằng pandas trong bản thiết kế gốc.
 *
 * Định dạng cột kỳ vọng:
 *  - File "Backlink hiện có": Domain ascore | Domain | Backlinks | Country
 *  - File "Backlink Gap":     Domain | MaxAS | Số đối thủ đang có | Ví dụ đối thủ sở hữu
 *
 * Dòng đầu tiên (header) sẽ bị bỏ qua.
 */
public class ExcelImportService {

    private final BacklinkDAO backlinkDAO = new BacklinkDAO();
    private final GapDomainDAO gapDomainDAO = new GapDomainDAO();

    /** Nhập file Excel "Backlink hiện có" — xoá dữ liệu cũ và nạp dữ liệu mới. */
    public int importBacklinks(String filePath) throws IOException {
        int count = 0;
        try (InputStream is = new FileInputStream(filePath);
             Workbook wb = WorkbookFactory.create(is)) {

            Sheet sheet = wb.getSheetAt(0);
            backlinkDAO.deleteAll();

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) continue;

                int ascore = (int) getNumeric(row.getCell(0));
                String domain = getString(row.getCell(1));
                int backlinks = (int) getNumeric(row.getCell(2));
                String country = getString(row.getCell(3));

                if (domain.isEmpty()) continue;

                backlinkDAO.insert(new BacklinkDomain(domain, ascore, backlinks, country));
                count++;
            }
        }
        return count;
    }

    /** Nhập file Excel "Backlink Gap" — tự tính priority_score cho từng dòng. */
    public int importGapDomains(String filePath) throws IOException {
        int count = 0;
        try (InputStream is = new FileInputStream(filePath);
             Workbook wb = WorkbookFactory.create(is)) {

            Sheet sheet = wb.getSheetAt(0);
            gapDomainDAO.deleteAll();

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) continue;

                String domain = getString(row.getCell(0));
                int maxAs = (int) getNumeric(row.getCell(1));
                int competitorCount = (int) getNumeric(row.getCell(2));
                String competitors = getString(row.getCell(3));

                if (domain.isEmpty()) continue;

                double score = GapDomain.calculatePriorityScore(maxAs, competitorCount);
                gapDomainDAO.insert(new GapDomain(domain, maxAs, competitorCount, competitors, score));
                count++;
            }
        }
        return count;
    }

    private boolean isRowEmpty(Row row) {
        for (int c = 0; c < 4; c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private String getString(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        }
        return cell.toString().trim();
    }

    private double getNumeric(Cell cell) {
        if (cell == null) return 0;
        try {
            if (cell.getCellType() == CellType.STRING) {
                String s = cell.getStringCellValue().trim();
                return s.isEmpty() ? 0 : Double.parseDouble(s);
            }
            return cell.getNumericCellValue();
        } catch (Exception e) {
            return 0;
        }
    }
}
