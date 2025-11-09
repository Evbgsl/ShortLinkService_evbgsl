package com.evbgsl.shortlinkservice.service;

import com.evbgsl.shortlinkservice.model.*;
import com.evbgsl.shortlinkservice.util.*;

import java.awt.*;
import java.net.URI;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class LinkService {

    private final NotificationService notifier = new NotificationService();

    public void createShortLinkWithNotification(String originalUrl, User user, int maxVisits, long ttlMinutes) {
        long maxTtl = AppConfig.ttlMinutes(); // читаем максимум из конфигурации

        // ограничиваем TTL от 1 до maxTtl
        ttlMinutes = Math.min(Math.max(ttlMinutes, 1), maxTtl);

        String code = LinkGenerator.generateCode();
        ShortLink shortLink = new ShortLink(code, originalUrl, maxVisits, ttlMinutes);
        user.addLink(shortLink);
        JsonStorage.saveLinks(user.getId(), user.getLinks());

        // уведомления о созданной ссылке
        notifier.info("Короткая ссылка создана! Код: " + code + " | Лимит переходов: " + maxVisits + " | TTL: " + ttlMinutes + " мин");
    }

    public void openLinkWithNotification(String code, User user) {
        Optional<ShortLink> linkOpt = user.getLinks().stream()
                .filter(l -> l.getShortCode().equals(code))
                .findFirst();


        if (linkOpt.isEmpty()) {
            notifier.error("Ссылка с таким кодом не найдена.");
            return;
        }


        ShortLink link = linkOpt.get();


        if (link.isExpired()) {
            notifier.warn("Срок жизни ссылки истёк. Ссылка будет удалена при ближайшей очистке.");
            return;
        }


        if (link.isLimitReached()) {
            notifier.warn(String.format("Лимит переходов исчерпан (%d/%d).",
                    link.getVisitCount(), link.getMaxVisits()));
            return;
        }


        // превентивные уведомления
        long remainingClicks = link.getMaxVisits() - link.getVisitCount();
        if (remainingClicks == 1) {
            notifier.warn(String.format("Остался 1 переход (%d/%d).",
                    link.getVisitCount(), link.getMaxVisits()));
        }
        long minutesLeft = link.getRemaining().toMinutes();
        if (minutesLeft > 0 && minutesLeft <= 60) {
            notifier.warn("Ссылка истекает через " + minutesLeft + " мин.");
        }


        try {
            Desktop.getDesktop().browse(new URI(link.getOriginalUrl()));
            link.incrementVisits();
            JsonStorage.saveLinks(user.getId(), user.getLinks());
            notifier.info(String.format("Переход выполнен! (%d/%d)",
                    link.getVisitCount(), link.getMaxVisits()));
        } catch (Exception e) {
            notifier.error("Ошибка при открытии ссылки: " + e.getMessage());
        }
    }

    public void cleanupExpiredWithNotification(User user) {
        int before = user.getLinks().size();
        user.getLinks().removeIf(ShortLink::isExpired);
        int removed = before - user.getLinks().size();


        if (removed > 0) {
            JsonStorage.saveLinks(user.getId(), user.getLinks());
            notifier.info("🗑 Удалено протухших ссылок: " + removed);
        }
    }

    // улучшенный вывод ссылок с уведомлениями
    public void listUserLinks(User user) {
        if (user.getLinks().isEmpty()) {
            System.out.println("У вас пока нет созданных ссылок.");
            return;
        }

        System.out.println("\nВаши ссылки:");

        // Создаём форматтер один раз для всех ссылок
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        user.getLinks().forEach(l -> {
            // Статус ссылки: протухла, лимит исчерпан, ок
            String status;
            if (l.isExpired()) {
                status = "Время жизни ссылки истекло";
            } else if (l.isLimitReached()) {
                status = "Лимит";
            } else {
                status = "OK";
            }

            // Вычисляем оставшееся время
            Duration left = l.getRemaining();
            long totalMinutes = Math.max(0, left.toMinutes());
            long hours = totalMinutes / 60;
            long minutes = totalMinutes % 60;
            String leftText = String.format("%dh %dm", hours, minutes);

            // Предупреждения: почти истек TTL или остался 1 переход
            String warnings = "";

            if (!l.isExpired() && totalMinutes > 0 && totalMinutes <= 60) warnings += "Время жизни ссылки скоро истечёт! ";
            if (!l.isLimitReached() && (l.getMaxVisits() - l.getVisitCount() == 1)) warnings += "Остался 1 переход!";
            if (totalMinutes > 0 && totalMinutes <= 60) warnings += "Скоро истечёт! ";

            String warningsText = warnings.isEmpty() ? "" : " " + warnings;

            System.out.printf("[%s] Код: %s | URL: %s | Клики: %d/%d | Срок жизни (осталось): %s | Создана: %s | Истекает: %s%s%n",
                    status,
                    l.getShortCode(),
                    l.getOriginalUrl(),
                    l.getVisitCount(),
                    l.getMaxVisits(),
                    leftText,
                    l.getCreatedAt().format(dtf),
                    l.getExpiresAt().format(dtf),
                    warningsText
            );
        });
    }

    // Загружаем ссылки пользователя из файла
    public void loadUserLinks(User user) {
        List<ShortLink> loaded = JsonStorage.loadLinks(user.getId());
        user.getLinks().addAll(loaded);
        if (!loaded.isEmpty()) {
            System.out.println("В вашем профиле есть ссылки в количестве" + loaded.size() + " шт.");
        }
    }
}
