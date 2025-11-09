package com.evbgsl.shortlinkservice.service;

import com.evbgsl.shortlinkservice.model.User;

import java.io.*;
import java.util.*;

public class UserService {

    private static final String USER_FILE = "user.id";
    private User currentUser;
    private final Map<UUID, User> users = new HashMap<>();

    public UserService() {
        this.currentUser = loadOrCreateUser();
        users.put(currentUser.getId(), currentUser);
    }

    private User loadOrCreateUser() {
        try {
            File file = new File(USER_FILE);
            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String uuidStr = reader.readLine();
                    UUID id = UUID.fromString(uuidStr);
                    System.out.println("Ваш персональный идентификатор пользователя (UUID) загружен: " + id);
                    return new User(id);
                }
            } else {
                User newUser = new User();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    writer.write(newUser.getId().toString());
                }
                System.out.println(
                        "Новый персональный идентификатор пользователя (UUID) создан: " + newUser.getId());
                return newUser;
            }
        } catch (IOException e) {
            System.err.println("Ошибка при работе с файлом UUID: " + e.getMessage());
            return new User();
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
