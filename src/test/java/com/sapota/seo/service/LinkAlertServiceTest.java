package com.sapota.seo.service;

import com.sapota.seo.model.CheckResult;
import com.sapota.seo.model.TrackedLink;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinkAlertServiceTest {

    private TrackedLink link(String url, String prev, String last) {
        TrackedLink t = new TrackedLink();
        t.setUrl(url);
        t.setPrevStatus(prev);
        t.setLastStatus(last);
        return t;
    }

    @Test
    void onlyLinksThatLostDofollowAreReported() {
        TrackedLink lost = link("https://a.com", CheckResult.STATUS_DOFOLLOW, CheckResult.STATUS_NOT_FOUND);
        TrackedLink ok = link("https://b.com", CheckResult.STATUS_DOFOLLOW, CheckResult.STATUS_DOFOLLOW);
        TrackedLink never = link("https://c.com", CheckResult.STATUS_NOFOLLOW, CheckResult.STATUS_NOFOLLOW);

        List<TrackedLink> result = LinkAlertService.findLost(List.of(lost, ok, never));
        assertEquals(1, result.size());
        assertEquals("https://a.com", result.get(0).getUrl());
    }

    @Test
    void transientAccessErrorDoesNotTriggerAlert() {
        TrackedLink err = link("https://a.com", CheckResult.STATUS_DOFOLLOW, CheckResult.STATUS_ERROR);
        assertTrue(LinkAlertService.findLost(List.of(err)).isEmpty());
    }

    @Test
    void noLinksMeansNoAlert() {
        assertTrue(LinkAlertService.findLost(List.of()).isEmpty());
    }

    @Test
    void emailContainsEveryLostUrlAndStatusChange() {
        TrackedLink a = link("https://a.com/post", CheckResult.STATUS_DOFOLLOW, CheckResult.STATUS_NOFOLLOW);
        a.setPlatform("Medium");
        TrackedLink b = link("https://b.com/post", CheckResult.STATUS_DOFOLLOW, CheckResult.STATUS_NOT_FOUND);

        String body = LinkAlertService.buildBody(List.of(a, b), "sapotacorp.vn");
        assertTrue(body.contains("https://a.com/post"));
        assertTrue(body.contains("https://b.com/post"));
        assertTrue(body.contains(CheckResult.STATUS_NOFOLLOW));
        assertTrue(body.contains("Medium"));
        assertTrue(body.contains("sapotacorp.vn"));
    }

    @Test
    void subjectMentionsCountAndDomain() {
        String subject = LinkAlertService.buildSubject(3, "sapotacorp.vn");
        assertTrue(subject.contains("3"));
        assertTrue(subject.contains("sapotacorp.vn"));
    }
}
