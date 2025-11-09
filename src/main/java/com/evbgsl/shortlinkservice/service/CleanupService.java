package com.evbgsl.shortlinkservice.service;

import com.evbgsl.shortlinkservice.model.User;
import java.util.concurrent.*;

public class CleanupService {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "ttl-cleaner");
        t.setDaemon(true); // чтобы не блокировать остановку приложения
        return t;
    });

    public void startPeriodic(User user, LinkService linkService) {
        // каждые 60 секунд пробуем очистить «протухшие» ссылки
        scheduler.scheduleAtFixedRate(() -> {
            try {
                linkService.cleanupExpiredWithNotification(user);
            }
            catch (Throwable t) {
                System.err.println("Ошибка в планировщике очистки: " + t.getMessage());
            }
        }, 10, 60, TimeUnit.SECONDS);
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}
