package com.sapota.seo.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Kiểm thử quy tắc phát hiện "mất/đổi link" của Backlink Monitor. */
class TrackedLinkTest {

    private TrackedLink link(String prev, String last) {
        TrackedLink t = new TrackedLink();
        t.setPrevStatus(prev);
        t.setLastStatus(last);
        return t;
    }

    @Test
    void dofollowToNofollowIsLost() {
        assertTrue(link(CheckResult.STATUS_DOFOLLOW, CheckResult.STATUS_NOFOLLOW).isLost());
    }

    @Test
    void dofollowToNotFoundIsLost() {
        assertTrue(link(CheckResult.STATUS_DOFOLLOW, CheckResult.STATUS_NOT_FOUND).isLost());
    }

    @Test
    void stillDofollowIsNotLost() {
        assertFalse(link(CheckResult.STATUS_DOFOLLOW, CheckResult.STATUS_DOFOLLOW).isLost());
    }

    @Test
    void neverWasDofollowIsNotLost() {
        assertFalse(link(CheckResult.STATUS_NOFOLLOW, CheckResult.STATUS_NOT_FOUND).isLost());
    }

    @Test
    void notCheckedYetIsNotLost() {
        assertFalse(link(null, null).isLost());
        assertFalse(link(CheckResult.STATUS_DOFOLLOW, null).isLost());
    }
}
