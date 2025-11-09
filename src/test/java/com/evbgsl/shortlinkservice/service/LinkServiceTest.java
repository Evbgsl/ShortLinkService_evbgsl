package com.evbgsl.shortlinkservice.service;

import static org.junit.jupiter.api.Assertions.*;

import com.evbgsl.shortlinkservice.model.ShortLink;
import com.evbgsl.shortlinkservice.model.User;
import com.evbgsl.shortlinkservice.util.JsonStorage;
import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.util.Comparator;
import java.util.stream.Stream;
import org.junit.jupiter.api.*;

class LinkServiceTest {

  private Path tempDir;
  private User user;
  private LinkService service;

  @BeforeEach
  void setup() throws IOException {
    // создаём временную папку для тестовых JSON
    tempDir = Files.createTempDirectory("shortlinkservice-test");

    // фиксируем UUID, чтобы JSON был уникальным для теста
    user = new User();

    // передаём путь временной папки в JsonStorage
    JsonStorage.setBaseDir(tempDir);

    service = new LinkService();
  }

  // чистим мусор после тестов
  @AfterEach
  void cleanup() {
    if (tempDir != null && Files.exists(tempDir)) {
      try (Stream<Path> stream = Files.walk(tempDir)) {
        stream
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(
                f -> {
                  if (!f.delete()) f.deleteOnExit();
                });
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Test
  void testCreateShortLinkWithNotification() {
    String code = service.createShortLinkWithNotification("https://example.com", user, 5, 60);
    assertNotNull(code);

    ShortLink link = user.getLinks().get(0);
    assertEquals("https://example.com", link.getOriginalUrl());
    assertEquals(5, link.getMaxVisits());
    assertEquals(60, link.getLifetimeMinutes());
  }

  @Test
  void testLimitReached() {
    ShortLink link = new ShortLink("ABC123", "https://example.com", 2, 60);
    user.addLink(link);

    assertFalse(link.isLimitReached());
    link.incrementVisits();
    assertFalse(link.isLimitReached());
    link.incrementVisits();
    assertTrue(link.isLimitReached());
  }

  @Test
  void testTTLExpiration() throws InterruptedException {
    ShortLink link = new ShortLink("TTL1", "https://example.com", 2, 1); // 1 минута TTL
    user.addLink(link);

    assertFalse(link.isExpired());
    Thread.sleep(65 * 1000); // ждем чуть больше минуты
    assertTrue(link.isExpired());
  }

  @Test
  void testRemainingTimeCalculation() {
    ShortLink link = new ShortLink("REM", "https://example.com", 3, 120); // 120 мин TTL
    user.addLink(link);

    Duration remaining = link.getRemaining();
    assertTrue(remaining.toMinutes() <= 120 && remaining.toMinutes() > 0);

    assertEquals(link.getCreatedAt().plusMinutes(120), link.getExpiresAt());
  }

  @Test
  void testWarningLastVisit() {
    ShortLink link = new ShortLink("WARN1", "https://example.com", 2, 60);
    user.addLink(link);

    link.incrementVisits(); // 1/2
    assertEquals(1, link.getMaxVisits() - link.getVisitCount());
  }

  @Test
  void testMultipleLinksForUser() {
    service.createShortLinkWithNotification("https://example.com/1", user, 3, 30);
    service.createShortLinkWithNotification("https://example.com/2", user, 2, 45);

    assertEquals(2, user.getLinks().size());
    assertEquals(3, user.getLinks().get(0).getMaxVisits());
    assertEquals(2, user.getLinks().get(1).getMaxVisits());
  }
}
