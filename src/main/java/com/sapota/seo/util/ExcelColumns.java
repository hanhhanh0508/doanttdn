package com.sapota.seo.util;

import java.util.List;

/** Tìm vị trí cột theo tên tiêu đề (không phụ thuộc thứ tự cột, vì mỗi lần xuất file từ Semrush có thể khác nhau). */
public final class ExcelColumns {

    private ExcelColumns() {
    }

    /**
     * @return chỉ số cột đầu tiên có tiêu đề đúng bằng {@code name} (không phân biệt hoa thường, bỏ khoảng trắng thừa);
     *         không thấy thì trả về {@code fallback} (vị trí mặc định theo mẫu file cũ).
     */
    public static int find(List<String> headers, String name, int fallback) {
        if (headers != null && name != null) {
            String target = name.trim();
            for (int i = 0; i < headers.size(); i++) {
                String h = headers.get(i);
                if (h != null && h.trim().equalsIgnoreCase(target)) return i;
            }
        }
        return fallback;
    }
}
