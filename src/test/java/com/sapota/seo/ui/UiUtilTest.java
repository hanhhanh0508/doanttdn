package com.sapota.seo.ui;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UiUtilTest {

    @Test
    void cleanDomainStripsSchemeWwwAndPath() {
        assertEquals("topdev.vn", UiUtil.cleanDomain("https://www.TopDev.vn/viec-lam?x=1"));
        assertEquals("dev.to", UiUtil.cleanDomain("  http://dev.to  "));
        assertEquals("example.com", UiUtil.cleanDomain("example.com/abc"));
    }

    @Test
    void cleanDomainHandlesNullAndEmpty() {
        assertEquals("", UiUtil.cleanDomain(null));
        assertEquals("", UiUtil.cleanDomain("   "));
    }

    @Test
    void parseDateAcceptsBothFormats() {
        assertEquals(LocalDate.of(2026, 10, 23), UiUtil.parseDate("23/10/2026"));
        assertEquals(LocalDate.of(2026, 10, 23), UiUtil.parseDate("2026-10-23"));
    }

    @Test
    void parseDateEmptyReturnsNull() {
        assertNull(UiUtil.parseDate(""));
        assertNull(UiUtil.parseDate(null));
    }

    @Test
    void parseDateRejectsGarbage() {
        assertThrows(DateTimeParseException.class, () -> UiUtil.parseDate("31/02/2026"));
        assertThrows(DateTimeParseException.class, () -> UiUtil.parseDate("hôm nay"));
    }

    @Test
    void formatDateRoundTrip() {
        assertEquals("05/01/2026", UiUtil.formatDate(LocalDate.of(2026, 1, 5)));
        assertEquals("", UiUtil.formatDate(null));
    }

    @Test
    void parseIntSafeFallsBackToDefault() {
        assertEquals(42, UiUtil.parseIntSafe(" 42 ", 0));
        assertEquals(7, UiUtil.parseIntSafe("abc", 7));
    }
}
