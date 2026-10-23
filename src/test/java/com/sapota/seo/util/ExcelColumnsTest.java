package com.sapota.seo.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExcelColumnsTest {

    /** Tiêu đề thật của file sapotacorp_vn-backlinks_refdomains.xlsx (có cột IP Address xen giữa). */
    private final List<String> semrush = Arrays.asList(
            "Domain ascore", "Domain", "Backlinks", "IP Address", "Country", "First seen", "Last seen");

    @Test
    void findsCountryAfterIpAddressColumn() {
        assertEquals(4, ExcelColumns.find(semrush, "Country", 3));
    }

    @Test
    void findsBacklinksColumn() {
        assertEquals(2, ExcelColumns.find(semrush, "Backlinks", 99));
    }

    @Test
    void matchIsCaseInsensitiveAndIgnoresSpaces() {
        assertEquals(1, ExcelColumns.find(Arrays.asList("A", "  COUNTRY "), "country", 0));
    }

    @Test
    void usesFallbackWhenHeaderMissingOrNull() {
        assertEquals(3, ExcelColumns.find(Arrays.asList("x", "y"), "Country", 3));
        assertEquals(3, ExcelColumns.find(null, "Country", 3));
        assertEquals(3, ExcelColumns.find(Arrays.asList((String) null), "Country", 3));
    }
}
