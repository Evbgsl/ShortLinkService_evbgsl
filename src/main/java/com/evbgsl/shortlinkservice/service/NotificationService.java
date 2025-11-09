package com.evbgsl.shortlinkservice.service;

public class NotificationService {
    // уведомление информационное
    public void info(String msg) {
        System.out.println(msg);
    }

    // предупреждение
    public void warn(String msg) {
        System.out.println(msg);
    }

    // ошибка
    public void error(String msg) {
        System.out.println(msg);
    }
}
