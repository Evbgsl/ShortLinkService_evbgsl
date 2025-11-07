package com.evbgsl.shortlinkservice.util;

import java.security.SecureRandom; // для генерации случайных чисел с криптозащитой, лучше обычного Random

public class LinkGenerator {
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();

    // Генерирует строку из случайных символов
    public static String generateCode() {
        int length = 6;
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
