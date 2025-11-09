package com.evbgsl.shortlinkservice.util;

import com.evbgsl.shortlinkservice.model.ShortLink;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.json.*;

public class JsonStorage {

    // В JsonStorage
    private static Path BASE_DIR = Paths.get(""); // по умолчанию текущая папка

    public static void setBaseDir(Path dir) {
        BASE_DIR = dir;
    }

    private static Path getFilePath(UUID userId) {
        return BASE_DIR.resolve("links_" + userId + ".json");
    }

    public static void saveLinks(UUID userId, List<ShortLink> links) {
        JSONArray arr = new JSONArray();
        for (ShortLink l : links) {
            JSONObject obj = new JSONObject();
            obj.put("code", l.getShortCode());
            obj.put("url", l.getOriginalUrl());
            obj.put("maxClicks", l.getMaxVisits());
            obj.put("createdAt", l.getCreatedAt().toString());
            obj.put("lifetimeMinutes", l.getLifetimeMinutes());
            obj.put("visitCount", l.getVisitCount());
            arr.put(obj);
        }

        Path filePath = getFilePath(userId);
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write(arr.toString(2)); // 2 — отступы для читаемости
        }
        catch (IOException e) {
            System.err.println("Ошибка при сохранении ссылок: " + e.getMessage());
        }
    }

    public static List<ShortLink> loadLinks(UUID userId) {
        Path filePath = getFilePath(userId);
        List<ShortLink> links = new ArrayList<>();

        if (!Files.exists(filePath))
            return links;

        try {
            String content = Files.readString(filePath);
            JSONArray arr = new JSONArray(content);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                ShortLink link = new ShortLink(o.getString("code"), o.getString("url"), o.getInt("maxClicks"),
                        o.getLong("lifetimeMinutes"));
                link.setCreatedAt(o.getString("createdAt"));
                if (o.has("visitCount")) {
                    link.setVisitCount(o.getInt("visitCount"));
                }
                links.add(link);
            }
        }
        catch (IOException e) {
            System.err.println("Ошибка при загрузке ссылок: " + e.getMessage());
        }

        return links;
    }
}
