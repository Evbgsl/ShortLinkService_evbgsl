package com.evbgsl.shortlinkservice;

import com.evbgsl.shortlinkservice.service.*;
import com.evbgsl.shortlinkservice.model.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        UserService userService = new UserService();
        User user = userService.getCurrentUser();
        LinkService linkService = new LinkService();
        linkService.loadUserLinks(user);

        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Сервис сокращения ссылок (UUID: " + user.getId() + ") ===");

        while (true) {
            System.out.println("\nМеню:");
            System.out.println("1. Создать короткую ссылку");
            System.out.println("2. Показать мои ссылки");
            System.out.println("3. Перейти по короткому коду");
            System.out.println("4. Очистить просроченные ссылки");
            System.out.println("5. Выход");
            System.out.print("Ваш выбор: ");

            String input = scanner.nextLine(); // ← читаем строку вместо числа

            // пробуем преобразовать в число
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

                    System.out.print("Введите лимит переходов (целое число ≥ 1):");
                    String limitStr = scanner.nextLine();
                    int maxVisits;

                    try {
                        maxVisits = Integer.parseInt(limitStr.trim());
                        if (maxVisits < 1) {
                            System.out.println("Лимит должен быть ≥ 1.");
                            break;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Введите целое число, например 5.");
                        break;
                    }

                    try {
                        String code = linkService.createShortLink(url, user, maxVisits);
                        System.out.println("Короткая ссылка создана!");
                        System.out.println("Код: " + code + " | Лимит: " + maxVisits);
                    } catch (IllegalArgumentException ex) {
                        System.out.println("Ошибка: " + ex.getMessage());
                    }
                }
                case 2 -> linkService.listUserLinks(user);
                case 3 -> {
                    System.out.print("Введите короткий код: ");
                    String code = scanner.nextLine();
                    linkService.openLink(code, user);
                }
                case 4 -> linkService.cleanupExpired(user);
                case 5 -> {
                    System.out.println("Выход из программы...");
                    return;
                }
                default -> System.out.println("Неверный выбор, попробуйте снова.");
            }
        }
    }
}
