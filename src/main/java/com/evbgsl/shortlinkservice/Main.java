package com.evbgsl.shortlinkservice;

import com.evbgsl.shortlinkservice.service.*;
import com.evbgsl.shortlinkservice.model.*;
import com.evbgsl.shortlinkservice.util.AppConfig;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("\n");
        System.out.println("=== Сервис сокращения ссылок ===");

        UserService userService = new UserService();
        User user = userService.getCurrentUser();
        LinkService linkService = new LinkService();
        linkService.loadUserLinks(user);

        CleanupService cleaner = new CleanupService();
        cleaner.startPeriodic(user, linkService);

        Scanner scanner = new Scanner(System.in);

        while (true) {
            linkService.cleanupExpiredWithNotification(user);

            System.out.println("\nМеню:");
            System.out.println("1. Создать короткую ссылку");
            System.out.println("2. Показать мои ссылки");
            System.out.println("3. Перейти по короткой ссылке");
            System.out.println("4. Очистить просроченные ссылки (ручная очистка, автоочистка каждые 60 секунд)");
            System.out.println("5. Выход из программы");
            System.out.println("\n");
            System.out.print("Сделайте Ваш выбор (введите номер пункта меню): ");

            String input = scanner.nextLine();

            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число от 1 до 5.");
                continue;
            }

            switch (choice) {
                case 1 -> {
                    System.out.print("Введите оригинальный URL: ");
                    String url = scanner.nextLine();

                    System.out.print("Введите лимит переходов по ссылке (целое число от 1 до 10): ");
                    String limitStr = scanner.nextLine();

                    int maxVisits;

                    if (limitStr.isBlank()) {
                        maxVisits = AppConfig.maxVisits(); // подтягиваем из конфигурации
                    } else {
                        try {
                            maxVisits = Integer.parseInt(limitStr.trim());
                            if (maxVisits < 1) {
                                System.out.println("Лимит переходов должен быть ≥ 1. Используем значение по умолчанию - 10.");
                                maxVisits = AppConfig.maxVisits();
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Ошибка ввода. Используем значение по умолчанию - 10.");
                            maxVisits = AppConfig.maxVisits();
                        }
                    }

                    System.out.print("Введите TTL в минутах (не более 300 минут, Enter для значения по умолчанию - 180 минут): ");
                    String ttlStr = scanner.nextLine();
                    long ttlMinutes;

                    if (ttlStr.isBlank()) {
                        // пользователь не ввёл значение - берём из конфигурации
                        ttlMinutes = AppConfig.ttlMinutes();
                    } else {
                        try {
                            ttlMinutes = Long.parseLong(ttlStr.trim());
                            if (ttlMinutes < 1 || ttlMinutes > 300) {
                                System.out.println("TTL должен быть от 1 до 300 минут. Используем значение по умолчанию.");
                                ttlMinutes = AppConfig.ttlMinutes();
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Неверный ввод. Используем значение по умолчанию.");
                            ttlMinutes = AppConfig.ttlMinutes();
                        }
                    }

                    // создаём ссылку через метод LinkService
                    try {
                        linkService.createShortLinkWithNotification(url, user, maxVisits, ttlMinutes);
                    } catch (IllegalArgumentException ex) {
                        System.out.println("Ошибка: " + ex.getMessage());
                    }
                }
                case 2 -> linkService.listUserLinks(user);
                case 3 -> {
                    System.out.print("Введите короткий код (его можно скопировать из колонки Код в списке ваших ссылок (Ctrl + Ins)): ");
                    String code = scanner.nextLine();
                    linkService.openLinkWithNotification(code, user);
                }
                case 4 -> linkService.cleanupExpiredWithNotification(user);
                case 5 -> {
                    System.out.println("Выход из программы...");
                    cleaner.shutdown();
                    return;
                }
                default -> System.out.println("Неверный выбор, попробуйте снова.");
            }
        }
    }
}
