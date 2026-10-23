package com.sapota.seo.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Kiểm thử công thức điểm ưu tiên và phân mức ưu tiên (mục 4.3.4 báo cáo). */
class GapDomainTest {

    @Test
    @DisplayName("Điểm = AS x trọng số AS + số đối thủ x trọng số đối thủ (trọng số mặc định 1.0 và 5.0)")
    void scoreWithDefaultWeights() {
        // topdev.vn trong dữ liệu mẫu: AS 41, 11 đối thủ -> 41 + 55 = 96
        assertEquals(96.0, GapDomain.calculatePriorityScore(41, 11, 1.0, 5.0), 0.0001);
    }

    @Test
    @DisplayName("Đổi trọng số thì điểm thay đổi tương ứng")
    void scoreWithCustomWeights() {
        assertEquals(41 * 2.0 + 11 * 1.0, GapDomain.calculatePriorityScore(41, 11, 2.0, 1.0), 0.0001);
    }

    @Test
    @DisplayName("Không có đối thủ và AS = 0 thì điểm = 0")
    void zeroInputs() {
        assertEquals(0.0, GapDomain.calculatePriorityScore(0, 0, 1.0, 5.0), 0.0001);
    }

    @ParameterizedTest(name = "điểm {0} -> {1}")
    @CsvSource({
            "150.0, Rất cao",
            "100.0, Rất cao",
            "99.99, Cao",
            "80.0,  Cao",
            "79.99, Trung bình",
            "60.0,  Trung bình",
            "59.99, Thấp",
            "0.0,   Thấp"
    })
    void priorityLevelBoundaries(double score, String expected) {
        assertEquals(expected, GapDomain.levelFor(score));
    }

    @Test
    void getPriorityLevelUsesStoredScore() {
        GapDomain g = new GapDomain("grokipedia.com", 65, 9, "a, b", 110.0);
        assertEquals("Rất cao", g.getPriorityLevel());
    }
}
