package com.evbgsl.shortlinkservice.model;

import java.util.*;

public class User {

    // Уникальный идентификатор пользователя (создаётся один раз при создании объекта)
    private final UUID id;
    // Список ссылок, принадлежащих пользователю
    private final List<ShortLink> links;

    public User() {
        this.id = UUID.randomUUID();
        this.links = new ArrayList<>();
    }

    public User(UUID id) {
        this.id = id;
        this.links = new ArrayList<>();
    }

    // Возвращает UUID пользователя
    public UUID getId() {
        return id;
    }

    // Возвращает список коротких ссылок пользователя
    public List<ShortLink> getLinks() {
        return links;
    }

    public void addLink(ShortLink link) {
        links.add(link);
    }
}
