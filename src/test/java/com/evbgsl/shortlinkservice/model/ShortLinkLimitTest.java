package com.evbgsl.shortlinkservice.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ShortLinkLimitTest {

    @Test
    void whenBelowLimit_thenNotReached() {
        ShortLink link = new ShortLink("ABC123", "https://example.com", 2, 24);
        assertFalse(link.isLimitReached());
        link.incrementVisits(); // 1/2
        assertFalse(link.isLimitReached());
    }

    @Test
    void whenEqualToLimit_thenReached() {
        ShortLink link = new ShortLink("ABC123", "https://example.com", 1, 24);
        assertFalse(link.isLimitReached());
        link.incrementVisits(); // 1/1
        assertTrue(link.isLimitReached());
    }

    @Test
    void zeroOrNegativeLimitShouldBeRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ShortLink("X", "https://example.com", 0, 24));
        assertThrows(IllegalArgumentException.class, () -> new ShortLink("X", "https://example.com", -5, 24));
    }
}
