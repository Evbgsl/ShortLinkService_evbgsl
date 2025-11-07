package com.evbgsl.shortlinkservice.service;

import com.evbgsl.shortlinkservice.model.*;
import com.evbgsl.shortlinkservice.util.*;

import java.awt.*;
import java.net.URI;
//import java.time.Duration;
import java.util.*;
import java.util.List;

public class LinkService {

    public void loadUserLinks(User user) {
        List<ShortLink> loaded = JsonStorage.loadLinks(user.getId());
        user.getLinks().addAll(loaded);
        if (!loaded.isEmpty())
            System.out.println("🔁 Загружено ссылок: " + loaded.size());
    }

    public String createShortLink(String originalUrl, User user, int maxVisits) {

        if (maxVisits < 1) {
            throw new IllegalArgumentException("Лимит должен быть положительным числом.");
        }

        String code = LinkGenerator.generateCode();
        ShortLink shortLink = new ShortLink(code, originalUrl, maxVisits, 24);
        user.addLink(shortLink);

        // сохраняем все ссылки этого пользователя
        JsonStorage.saveLinks(user.getId(), user.getLinks());

        return code;
    }


    // Оставим перегрузку метода на будущее (со значением maxVisits по умолчанию)
    public String createShortLink(String originalUrl, User user) {
        return createShortLink(originalUrl, user, 10); // дефолт 10, если не указали
    }


    public void listUserLinks(User user) {
        if (user.getLinks().isEmpty()) {
            System.out.println("У вас пока нет созданных ссылок.");
            return;
        }
        System.out.println("\nВаши ссылки:");
        user.getLinks().forEach(l ->
                System.out.printf("Код: %s | URL: %s | Клики: %d/%d | Создана: %s%n",
                        l.getShortCode(),
                        l.getOriginalUrl(),
                        l.getVisitCount(),
                        l.getMaxVisits(),
                        l.getCreatedAt())
        );
    }

    // ✅ переход по короткому коду
    public void openLink(String code, User user) {
        Optional<ShortLink> linkOpt = user.getLinks().stream()
                .filter(l -> l.getShortCode().equals(code))
                .findFirst();

        if (linkOpt.isEmpty()) {
            System.out.println("Ссылка с таким кодом не найдена!");
            return;
        }

        ShortLink link = linkOpt.get();

        if (link.isExpired()) {
            System.out.println("Срок жизни ссылки истёк!");
            return;
        }

        if (link.isLimitReached()) {
            System.out.printf("Лимит переходов исчерпан (%d/%d).%n", link.getVisitCount(), link.getMaxVisits());
            return;
        }

        try {
            Desktop.getDesktop().browse(new URI(link.getOriginalUrl()));
            link.incrementVisits();
            JsonStorage.saveLinks(user.getId(), user.getLinks());
            System.out.printf("Переход выполнен! (%d/%d)%n", link.getVisitCount(), link.getMaxVisits());
        } catch (Exception e) {
            System.err.println("Ошибка при открытии ссылки: " + e.getMessage());
        }
    }

    // удаление “протухших” ссылок
    public void cleanupExpired(User user) {
        int before = user.getLinks().size();
        user.getLinks().removeIf(ShortLink::isExpired);
        int removed = before - user.getLinks().size();

        if (removed > 0) {
            JsonStorage.saveLinks(user.getId(), user.getLinks());
            System.out.println("Удалено протухших ссылок: " + removed);
        }
    }


}
