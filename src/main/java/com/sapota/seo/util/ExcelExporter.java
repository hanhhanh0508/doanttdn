package com.sapota.seo.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.JTable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Xuất dữ liệu ra file Excel (.xlsx) bằng Apache POI — có định dạng tiêu đề, số, độ rộng cột. */
public final class ExcelExporter {

    private ExcelExporter() {
    }

    /** Xuất bảng đang hiển thị (đúng thứ tự sắp xếp/lọc người dùng đang xem) ra 1 sheet. */
    public static void exportTable(JTable table, String sheetName, String filePath) throws IOException {
        List<String> headers = new ArrayList<>();
        for (int c = 0; c < table.getColumnCount(); c++) headers.add(table.getColumnName(c));
        List<Object[]> rows = new ArrayList<>();
        for (int r = 0; r < table.getRowCount(); r++) {
            Object[] row = new Object[table.getColumnCount()];
            for (int c = 0; c < row.length; c++) row[c] = table.getValueAt(r, c);
            rows.add(row);
        }
        try (Workbook wb = new XSSFWorkbook()) {
            writeSheet(wb, sheetName, headers, rows);
            save(wb, filePath);
        }
    }

    public static void save(Workbook wb, String filePath) throws IOException {
        String path = filePath.toLowerCase().endsWith(".xlsx") ? filePath : filePath + ".xlsx";
        try (FileOutputStream out = new FileOutputStream(path)) {
            wb.write(out);
        }
    }

    public static void writeSheet(Workbook wb, String sheetName, List<String> headers, List<Object[]> rows) {
        Sheet sheet = wb.createSheet(sheetName);

        CellStyle headStyle = wb.createCellStyle();
        Font bold = wb.createFont();
        bold.setBold(true);
        headStyle.setFont(bold);
        headStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Row head = sheet.createRow(0);
        for (int c = 0; c < headers.size(); c++) {
            Cell cell = head.createCell(c);
            cell.setCellValue(headers.get(c));
            cell.setCellStyle(headStyle);
        }

        for (int r = 0; r < rows.size(); r++) {
            Row row = sheet.createRow(r + 1);
            Object[] data = rows.get(r);
            for (int c = 0; c < data.length; c++) {
                Cell cell = row.createCell(c);
                Object v = data[c];
                if (v instanceof Number) cell.setCellValue(((Number) v).doubleValue());
                else cell.setCellValue(v == null ? "" : v.toString());
            }
        }

        sheet.createFreezePane(0, 1);
        for (int c = 0; c < headers.size(); c++) {
            sheet.setColumnWidth(c, widthFor(headers.get(c), rows, c));
        }
    }

    /** Ước lượng độ rộng cột theo độ dài chữ (không dùng autoSizeColumn để tránh phụ thuộc font hệ thống). */
    private static int widthFor(String header, List<Object[]> rows, int col) {
        int max = header.length();
        int limit = Math.min(rows.size(), 200);
        for (int i = 0; i < limit; i++) {
            Object v = rows.get(i)[col];
            if (v != null) max = Math.max(max, v.toString().length());
        }
        return Math.min(max + 3, 60) * 256;
    }
}
