package com.sapota.seo.service;

import com.sapota.seo.model.CheckResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Kiểm thử phần phân loại Dofollow/Nofollow bằng HTML mẫu (không cần mạng). */
class DofollowCheckerServiceTest {

    private static final String TARGET = "sapotacorp.vn";
    private static final String URL = "https://example.com/bai-viet";

    private CheckResult classify(String bodyHtml) {
        Document doc = Jsoup.parse("<html><body>" + bodyHtml + "</body></html>", URL);
        return DofollowCheckerService.classify(URL, doc, TARGET);
    }

    @Test
    void linkWithoutRelIsDofollow() {
        CheckResult r = classify("<a href=\"https://sapotacorp.vn/\">Sapota</a>");
        assertEquals(CheckResult.STATUS_DOFOLLOW, r.getStatus());
    }

    @Test
    void relNofollowIsNofollow() {
        CheckResult r = classify("<a href=\"https://sapotacorp.vn/\" rel=\"nofollow\">Sapota</a>");
        assertEquals(CheckResult.STATUS_NOFOLLOW, r.getStatus());
    }

    @Test
    void nofollowAmongOtherRelValuesIsStillNofollow() {
        CheckResult r = classify("<a href=\"https://sapotacorp.vn/\" rel=\"noopener noreferrer nofollow\">Sapota</a>");
        assertEquals(CheckResult.STATUS_NOFOLLOW, r.getStatus());
    }

    @Test
    void relIsCaseInsensitive() {
        CheckResult r = classify("<a href=\"https://sapotacorp.vn/\" rel=\"NoFollow\">Sapota</a>");
        assertEquals(CheckResult.STATUS_NOFOLLOW, r.getStatus());
    }

    @Test
    void relNoopenerOnlyIsDofollow() {
        CheckResult r = classify("<a href=\"https://sapotacorp.vn/\" rel=\"noopener\">Sapota</a>");
        assertEquals(CheckResult.STATUS_DOFOLLOW, r.getStatus());
    }

    @Test
    void oneDofollowAmongNofollowLinksMakesPageDofollow() {
        CheckResult r = classify(
                "<a href=\"https://sapotacorp.vn/a\" rel=\"nofollow\">1</a>"
              + "<a href=\"https://sapotacorp.vn/b\">2</a>");
        assertEquals(CheckResult.STATUS_DOFOLLOW, r.getStatus());
        assertTrue(r.getNote().contains("2"), "ghi chú phải nêu số liên kết tìm thấy");
    }

    @Test
    void allNofollowLinksMakePageNofollow() {
        CheckResult r = classify(
                "<a href=\"https://sapotacorp.vn/a\" rel=\"nofollow\">1</a>"
              + "<a href=\"https://sapotacorp.vn/b\" rel=\"nofollow\">2</a>");
        assertEquals(CheckResult.STATUS_NOFOLLOW, r.getStatus());
    }

    @Test
    void pageWithoutTargetLinkIsNotFound() {
        CheckResult r = classify("<a href=\"https://other-site.com/\">Khác</a><p>sapotacorp.vn chỉ là chữ, không phải link</p>");
        assertEquals(CheckResult.STATUS_NOT_FOUND, r.getStatus());
    }

    @Test
    void emptyPageIsNotFound() {
        assertEquals(CheckResult.STATUS_NOT_FOUND, classify("").getStatus());
    }

    @Test
    void resultKeepsUrl() {
        assertEquals(URL, classify("<a href=\"https://sapotacorp.vn/\">x</a>").getUrl());
    }

    @Test
    void customTargetDomainIsRespected() {
        Document doc = Jsoup.parse("<a href=\"https://example.org/x\">x</a>", URL);
        assertEquals(CheckResult.STATUS_DOFOLLOW, DofollowCheckerService.classify(URL, doc, "example.org").getStatus());
        assertEquals(CheckResult.STATUS_NOT_FOUND, DofollowCheckerService.classify(URL, doc, TARGET).getStatus());
    }

    @Test
    void emptyUrlReturnsErrorWithoutNetworkCall() {
        CheckResult r = new DofollowCheckerService(TARGET).check("   ");
        assertEquals(CheckResult.STATUS_ERROR, r.getStatus());
    }
}
