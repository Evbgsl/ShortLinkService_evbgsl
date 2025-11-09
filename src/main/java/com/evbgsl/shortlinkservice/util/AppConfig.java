package com.evbgsl.shortlinkservice.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final Properties PROPS = new Properties();


    static {
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("app.properties")) {
            if (in != null) {
                PROPS.load(in);
            } else {
                System.err.println("app.properties не найден в classpath. Будут использованы значения по умолчанию.");
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения app.properties: " + e.getMessage());
        }
    }

    public static long ttlHours() {
        String v = PROPS.getProperty("ttl.hours", "24");
        try {
            return Long.parseLong(v);
        } catch (NumberFormatException e) {
            System.err.println("Неверное значение ttl.hours, используется 24");
            return 24L;
        }
    }

    public static long ttlMinutes() {
        String v = PROPS.getProperty("ttl.minutes", "60"); // по умолчанию 60 минут
        try {
            return Long.parseLong(v);
        } catch (NumberFormatException e) {
            System.err.println("Неверное значение ttl.minutes, используется 60");
            return 60L;
        }
    }

    public static int maxVisits() {
        String v = PROPS.getProperty("max.visits", "10");
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            System.err.println("Неверное значение max.visits, используется 10");
            return 10;
        }
    }
}
