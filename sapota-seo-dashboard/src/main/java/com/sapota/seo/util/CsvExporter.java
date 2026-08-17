package com.sapota.seo.util;

import javax.swing.JTable;
import javax.swing.table.TableModel;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/** Tiện ích xuất dữ liệu từ JTable ra file CSV (mở được bằng Excel). */
public final class CsvExporter {

    private CsvExporter() {
    }

    public static void export(JTable table, String filePath) throws IOException {
        TableModel model = table.getModel();

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath, java.nio.charset.StandardCharsets.UTF_8))) {
            // Ghi BOM để Excel hiển thị đúng tiếng Việt có dấu
            writer.write('\uFEFF');

            // Header
            StringBuilder header = new StringBuilder();
            for (int c = 0; c < model.getColumnCount(); c++) {
                if (c > 0) header.append(",");
                header.append(escape(model.getColumnName(c)));
            }
            writer.println(header);

            // Dữ liệu
            for (int r = 0; r < model.getRowCount(); r++) {
                StringBuilder line = new StringBuilder();
                for (int c = 0; c < model.getColumnCount(); c++) {
                    if (c > 0) line.append(",");
                    Object value = model.getValueAt(r, c);
                    line.append(escape(value == null ? "" : value.toString()));
                }
                writer.println(line);
            }
        }
    }

    private static String escape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
