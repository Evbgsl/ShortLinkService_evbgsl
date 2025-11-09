package com.evbgsl.shortlinkservice.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import org.junit.jupiter.api.Test;

public class ShortLinkTest {

    @Test
    public void testIncrementVisits() {
        ShortLink link = new ShortLink("abc123", "https://example.com", 3, 60);
        assertEquals(0, link.getVisitCount());
        link.incrementVisits();
        assertEquals(1, link.getVisitCount());
    }

    @Test
    public void testIsLimitReached() {
        ShortLink link = new ShortLink("abc123", "https://example.com", 2, 60);
        assertFalse(link.isLimitReached());
        link.incrementVisits();
        assertFalse(link.isLimitReached());
        link.incrementVisits();
        assertTrue(link.isLimitReached());
    }

    @Test
    public void testIsExpired() throws InterruptedException {
        ShortLink link = new ShortLink("abc123", "https://example.com", 2, 1); // 1 минута
        assertFalse(link.isExpired());
        // имитируем истечение времени
        Thread.sleep(70 * 1000); // 70 секунд
        assertTrue(link.isExpired());
    }

    @Test
    public void testGetRemaining() {
        ShortLink link = new ShortLink("abc123", "https://example.com", 1, 120); // 120 минут
        Duration remaining = link.getRemaining();
        assertTrue(remaining.toMinutes() <= 120 && remaining.toMinutes() > 0);
    }

    @Test
    public void testGetExpiresAt() {
        ShortLink link = new ShortLink("abc123", "https://example.com", 1, 60);
        assertEquals(link.getCreatedAt().plusMinutes(60), link.getExpiresAt());
    }
}
