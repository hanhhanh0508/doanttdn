package com.sapota.seo.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class SchedulerServiceTest {

    @Test
    void beforeTargetTimeRunsLaterToday() {
        LocalDateTime now = LocalDateTime.of(2026, 10, 23, 7, 0, 0);
        assertEquals(3600, SchedulerService.secondsUntilNextRun(now, LocalTime.of(8, 0)));
    }

    @Test
    void afterTargetTimeRunsTomorrow() {
        LocalDateTime now = LocalDateTime.of(2026, 10, 23, 9, 0, 0);
        assertEquals(23 * 3600, SchedulerService.secondsUntilNextRun(now, LocalTime.of(8, 0)));
    }

    @Test
    void exactlyAtTargetTimeRunsTomorrowNotImmediately() {
        LocalDateTime now = LocalDateTime.of(2026, 10, 23, 8, 0, 0);
        assertEquals(24 * 3600, SchedulerService.secondsUntilNextRun(now, LocalTime.of(8, 0)));
    }

    @Test
    void crossesMidnightCorrectly() {
        LocalDateTime now = LocalDateTime.of(2026, 10, 23, 23, 30, 0);
        assertEquals(30 * 60 + 8 * 3600, SchedulerService.secondsUntilNextRun(now, LocalTime.of(8, 0)));
    }

    @Test
    void parseTimeAcceptsValidAndRejectsInvalid() {
        assertNotNull(SchedulerService.parseTime("08:30"));
        assertNotNull(SchedulerService.parseTime(" 23:59 "));
        assertNull(SchedulerService.parseTime("25:00"));
        assertNull(SchedulerService.parseTime("abc"));
        assertNull(SchedulerService.parseTime(null));
    }
}
