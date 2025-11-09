package com.evbgsl.shortlinkservice.model;

import java.time.*;

public class ShortLink {
    private final String shortCode;
    private final String originalUrl;
    private final int maxVisits;
    private final long lifetimeHours;
    private LocalDateTime createdAt;
    private int visitCount;

    public ShortLink(String shortCode, String originalUrl, int maxVisits, long lifetimeHours) {


        if (maxVisits < 1) {
            throw new IllegalArgumentException("Количество переходов должно быть >= 1");
        }

        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.maxVisits = maxVisits;
        this.lifetimeHours = lifetimeHours;
        this.createdAt = LocalDateTime.now();
        this.visitCount = 0;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public int getMaxVisits() {
        return maxVisits;
    }

    public long getLifetimeHours() {
        return lifetimeHours;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public int getVisitCount() {
        return visitCount;
    }


    // Позволяет восстановить дату из JSON
    public void setCreatedAt(String time) {
        this.createdAt = LocalDateTime.parse(time);
    }

    public void incrementVisits() {
        visitCount++;
    }

    // --- Проверки ---
    public boolean isExpired() {
        return Duration.between(createdAt, LocalDateTime.now()).toMinutes() >= lifetimeHours;
    }

    public boolean isLimitReached() {
        return visitCount >= maxVisits;
    }

    public void setVisitCount(int count) {
        this.visitCount = count;
    }

    public LocalDateTime getExpiresAt() {
        return createdAt.plusHours(lifetimeHours);
    }

    public Duration getRemaining() {
        return Duration.between(LocalDateTime.now(), getExpiresAt());
    }

}
