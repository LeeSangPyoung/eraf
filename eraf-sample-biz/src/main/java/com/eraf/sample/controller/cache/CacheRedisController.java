package com.eraf.sample.controller.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * 캐시/Redis 컨트롤러
 * #60 캐시 조회, #61 캐시 무효화
 * #62 Redis 데이터 조회, #63 Redis Pub/Sub 시연
 */
@Controller
@RequestMapping("/cache")
public class CacheRedisController {

    private static final Logger log = LoggerFactory.getLogger(CacheRedisController.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired(required = false)
    private CacheManager cacheManager;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    // In-memory simulation (when Redis is not available)
    private final Map<String, Map<String, CacheEntry>> mockCache = new ConcurrentHashMap<>();
    private final Map<String, String> mockRedisData = new ConcurrentHashMap<>();
    private final Map<String, List<PubSubMessage>> pubSubChannels = new ConcurrentHashMap<>();
    private final List<PubSubMessage> pubSubHistory = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void init() {
        // Initialize sample cache entries
        initSampleCacheData();
        initSampleRedisData();
    }

    private void initSampleCacheData() {
        // Sample cache: users
        Map<String, CacheEntry> usersCache = new ConcurrentHashMap<>();
        usersCache.put("user:1", new CacheEntry("user:1", "{\"id\":1,\"name\":\"John\",\"email\":\"john@example.com\"}", 3600));
        usersCache.put("user:2", new CacheEntry("user:2", "{\"id\":2,\"name\":\"Jane\",\"email\":\"jane@example.com\"}", 3600));
        usersCache.put("user:3", new CacheEntry("user:3", "{\"id\":3,\"name\":\"Bob\",\"email\":\"bob@example.com\"}", 3600));
        mockCache.put("users", usersCache);

        // Sample cache: products
        Map<String, CacheEntry> productsCache = new ConcurrentHashMap<>();
        productsCache.put("product:100", new CacheEntry("product:100", "{\"id\":100,\"name\":\"Laptop\",\"price\":1200000}", 7200));
        productsCache.put("product:101", new CacheEntry("product:101", "{\"id\":101,\"name\":\"Mouse\",\"price\":50000}", 7200));
        mockCache.put("products", productsCache);

        // Sample cache: sessions
        Map<String, CacheEntry> sessionsCache = new ConcurrentHashMap<>();
        sessionsCache.put("session:abc123", new CacheEntry("session:abc123", "{\"userId\":1,\"loginAt\":\"2024-01-15T10:00:00\"}", 1800));
        mockCache.put("sessions", sessionsCache);

        // Sample cache: settings
        Map<String, CacheEntry> settingsCache = new ConcurrentHashMap<>();
        settingsCache.put("app:config", new CacheEntry("app:config", "{\"theme\":\"dark\",\"language\":\"ko\"}", -1));
        mockCache.put("settings", settingsCache);
    }

    private void initSampleRedisData() {
        // Sample Redis data
        mockRedisData.put("config:app:name", "ERAF Sample");
        mockRedisData.put("config:app:version", "1.0.0");
        mockRedisData.put("counter:visits", "12345");
        mockRedisData.put("counter:api_calls", "67890");
        mockRedisData.put("lock:order:12345", "locked");
        mockRedisData.put("session:user:1", "{\"userId\":1,\"token\":\"abc123\"}");
        mockRedisData.put("rate_limit:api:user:1", "10/60");
    }

    @GetMapping
    public String cachePage() {
        return "cache/cache";
    }

    private boolean isRedisAvailable() {
        if (stringRedisTemplate == null) return false;
        try {
            stringRedisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ============ #60 캐시 조회 ============

    @GetMapping("/list")
    @ResponseBody
    public Map<String, Object> getCacheList() {
        boolean redisAvailable = isRedisAvailable();

        List<CacheInfo> caches = new ArrayList<>();

        if (cacheManager != null) {
            for (String cacheName : cacheManager.getCacheNames()) {
                Cache cache = cacheManager.getCache(cacheName);
                CacheInfo info = new CacheInfo();
                info.setName(cacheName);
                info.setType("Spring Cache");
                info.setEntryCount(mockCache.containsKey(cacheName) ? mockCache.get(cacheName).size() : 0);
                caches.add(info);
            }
        }

        // Add mock caches if not in CacheManager
        for (String cacheName : mockCache.keySet()) {
            if (caches.stream().noneMatch(c -> c.getName().equals(cacheName))) {
                CacheInfo info = new CacheInfo();
                info.setName(cacheName);
                info.setType("In-Memory (Mock)");
                info.setEntryCount(mockCache.get(cacheName).size());
                caches.add(info);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("redisAvailable", redisAvailable);
        result.put("cacheManagerType", cacheManager != null ? cacheManager.getClass().getSimpleName() : "None");
        result.put("caches", caches);
        result.put("totalCaches", caches.size());

        return result;
    }

    @GetMapping("/{cacheName}/entries")
    @ResponseBody
    public Map<String, Object> getCacheEntries(@PathVariable String cacheName) {
        Map<String, CacheEntry> cacheEntries = mockCache.get(cacheName);

        if (cacheEntries == null) {
            return Map.of("error", "Cache not found: " + cacheName, "entries", List.of());
        }

        List<CacheEntry> entries = new ArrayList<>(cacheEntries.values());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cacheName", cacheName);
        result.put("entryCount", entries.size());
        result.put("entries", entries);

        return result;
    }

    @PostMapping("/put")
    @ResponseBody
    public Map<String, Object> putCacheEntry(@RequestBody CachePutRequest request) {
        Map<String, CacheEntry> cacheEntries = mockCache.computeIfAbsent(request.getCacheName(), k -> new ConcurrentHashMap<>());

        CacheEntry entry = new CacheEntry(request.getKey(), request.getValue(), request.getTtlSeconds());
        cacheEntries.put(request.getKey(), entry);

        log.info("[Cache] Put entry: cache={}, key={}", request.getCacheName(), request.getKey());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("cacheName", request.getCacheName());
        result.put("key", request.getKey());
        result.put("message", "Cache entry added");

        return result;
    }

    // ============ #61 캐시 무효화 ============

    @DeleteMapping("/{cacheName}/entries/{key}")
    @ResponseBody
    public Map<String, Object> evictCacheEntry(@PathVariable String cacheName, @PathVariable String key) {
        Map<String, CacheEntry> cacheEntries = mockCache.get(cacheName);

        if (cacheEntries == null) {
            return Map.of("success", false, "error", "Cache not found: " + cacheName);
        }

        CacheEntry removed = cacheEntries.remove(key);

        log.info("[Cache] Evicted entry: cache={}, key={}", cacheName, key);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", removed != null);
        result.put("cacheName", cacheName);
        result.put("key", key);
        result.put("message", removed != null ? "Entry evicted" : "Entry not found");

        return result;
    }

    @DeleteMapping("/{cacheName}/clear")
    @ResponseBody
    public Map<String, Object> clearCache(@PathVariable String cacheName) {
        Map<String, CacheEntry> cacheEntries = mockCache.get(cacheName);

        if (cacheEntries == null) {
            return Map.of("success", false, "error", "Cache not found: " + cacheName);
        }

        int count = cacheEntries.size();
        cacheEntries.clear();

        log.info("[Cache] Cleared cache: {}, entries removed: {}", cacheName, count);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("cacheName", cacheName);
        result.put("entriesCleared", count);
        result.put("message", "Cache cleared");

        return result;
    }

    // ============ #62 Redis 데이터 조회 ============

    @GetMapping("/redis/keys")
    @ResponseBody
    public Map<String, Object> getRedisKeys(@RequestParam(defaultValue = "*") String pattern) {
        boolean redisAvailable = isRedisAvailable();
        List<RedisKeyInfo> keys = new ArrayList<>();

        if (redisAvailable) {
            try {
                Set<String> redisKeys = stringRedisTemplate.keys(pattern);
                if (redisKeys != null) {
                    for (String key : redisKeys) {
                        RedisKeyInfo info = new RedisKeyInfo();
                        info.setKey(key);
                        info.setType(String.valueOf(stringRedisTemplate.type(key)));
                        Long ttl = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
                        info.setTtl(ttl != null ? ttl : -1);
                        keys.add(info);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to get Redis keys: {}", e.getMessage());
            }
        }

        // Add mock data for demo
        for (String key : mockRedisData.keySet()) {
            if (pattern.equals("*") || key.contains(pattern.replace("*", ""))) {
                if (keys.stream().noneMatch(k -> k.getKey().equals(key))) {
                    RedisKeyInfo info = new RedisKeyInfo();
                    info.setKey(key);
                    info.setType("string");
                    info.setTtl(-1);
                    info.setMock(true);
                    keys.add(info);
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("redisAvailable", redisAvailable);
        result.put("pattern", pattern);
        result.put("keyCount", keys.size());
        result.put("keys", keys);

        return result;
    }

    @GetMapping("/redis/get")
    @ResponseBody
    public Map<String, Object> getRedisValue(@RequestParam String key) {
        boolean redisAvailable = isRedisAvailable();
        String value = null;
        Long ttl = null;
        boolean fromMock = false;

        if (redisAvailable) {
            try {
                value = stringRedisTemplate.opsForValue().get(key);
                ttl = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.warn("Failed to get Redis value: {}", e.getMessage());
            }
        }

        if (value == null && mockRedisData.containsKey(key)) {
            value = mockRedisData.get(key);
            ttl = -1L;
            fromMock = true;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("key", key);
        result.put("value", value);
        result.put("ttl", ttl);
        result.put("exists", value != null);
        result.put("source", fromMock ? "mock" : "redis");

        return result;
    }

    @PostMapping("/redis/set")
    @ResponseBody
    public Map<String, Object> setRedisValue(@RequestBody RedisSetRequest request) {
        boolean redisAvailable = isRedisAvailable();
        boolean success = false;

        if (redisAvailable) {
            try {
                if (request.getTtlSeconds() > 0) {
                    stringRedisTemplate.opsForValue().set(request.getKey(), request.getValue(),
                            Duration.ofSeconds(request.getTtlSeconds()));
                } else {
                    stringRedisTemplate.opsForValue().set(request.getKey(), request.getValue());
                }
                success = true;
            } catch (Exception e) {
                log.warn("Failed to set Redis value: {}", e.getMessage());
            }
        }

        // Also store in mock
        mockRedisData.put(request.getKey(), request.getValue());
        success = true;

        log.info("[Redis] Set key: {}", request.getKey());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", success);
        result.put("key", request.getKey());
        result.put("ttlSeconds", request.getTtlSeconds());
        result.put("redisAvailable", redisAvailable);

        return result;
    }

    @DeleteMapping("/redis/delete")
    @ResponseBody
    public Map<String, Object> deleteRedisKey(@RequestParam String key) {
        boolean redisAvailable = isRedisAvailable();
        boolean deleted = false;

        if (redisAvailable) {
            try {
                deleted = Boolean.TRUE.equals(stringRedisTemplate.delete(key));
            } catch (Exception e) {
                log.warn("Failed to delete Redis key: {}", e.getMessage());
            }
        }

        // Also remove from mock
        if (mockRedisData.remove(key) != null) {
            deleted = true;
        }

        log.info("[Redis] Deleted key: {}", key);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", deleted);
        result.put("key", key);
        result.put("redisAvailable", redisAvailable);

        return result;
    }

    // ============ #63 Redis Pub/Sub 시연 ============

    @PostMapping("/pubsub/subscribe")
    @ResponseBody
    public Map<String, Object> subscribeChannel(@RequestBody Map<String, String> request) {
        String channel = request.get("channel");

        pubSubChannels.computeIfAbsent(channel, k -> new CopyOnWriteArrayList<>());

        log.info("[Pub/Sub] Subscribed to channel: {}", channel);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("channel", channel);
        result.put("subscribed", true);
        result.put("activeChannels", pubSubChannels.keySet());

        return result;
    }

    @PostMapping("/pubsub/publish")
    @ResponseBody
    public Map<String, Object> publishMessage(@RequestBody PubSubPublishRequest request) {
        String channel = request.getChannel();
        String message = request.getMessage();

        boolean redisAvailable = isRedisAvailable();

        if (redisAvailable) {
            try {
                stringRedisTemplate.convertAndSend(channel, message);
            } catch (Exception e) {
                log.warn("Failed to publish to Redis: {}", e.getMessage());
            }
        }

        // Store in mock
        PubSubMessage pubSubMessage = new PubSubMessage();
        pubSubMessage.setMessageId(UUID.randomUUID().toString().substring(0, 8));
        pubSubMessage.setChannel(channel);
        pubSubMessage.setMessage(message);
        pubSubMessage.setTimestamp(LocalDateTime.now());

        pubSubHistory.add(0, pubSubMessage);
        if (pubSubChannels.containsKey(channel)) {
            pubSubChannels.get(channel).add(0, pubSubMessage);
        }

        log.info("[Pub/Sub] Published message to channel: {}", channel);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("messageId", pubSubMessage.getMessageId());
        result.put("channel", channel);
        result.put("timestamp", pubSubMessage.getTimestamp().format(FORMATTER));
        result.put("redisAvailable", redisAvailable);

        return result;
    }

    @GetMapping("/pubsub/channels")
    @ResponseBody
    public Map<String, Object> getChannels() {
        Map<String, Integer> channelStats = new LinkedHashMap<>();
        pubSubChannels.forEach((channel, messages) -> channelStats.put(channel, messages.size()));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("channels", channelStats);
        result.put("totalChannels", pubSubChannels.size());

        return result;
    }

    @GetMapping("/pubsub/messages")
    @ResponseBody
    public Map<String, Object> getMessages(
            @RequestParam(required = false) String channel,
            @RequestParam(defaultValue = "20") int limit) {

        List<PubSubMessage> messages;

        if (channel != null && !channel.isEmpty()) {
            messages = pubSubChannels.getOrDefault(channel, List.of())
                    .stream().limit(limit).toList();
        } else {
            messages = pubSubHistory.stream().limit(limit).toList();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("channel", channel != null ? channel : "ALL");
        result.put("messageCount", messages.size());
        result.put("messages", messages);

        return result;
    }

    @DeleteMapping("/pubsub/clear")
    @ResponseBody
    public Map<String, Object> clearPubSub() {
        int count = pubSubHistory.size();
        pubSubHistory.clear();
        pubSubChannels.clear();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("clearedMessages", count);

        return result;
    }

    // ============ DTOs ============

    public static class CacheInfo {
        private String name;
        private String type;
        private int entryCount;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public int getEntryCount() { return entryCount; }
        public void setEntryCount(int entryCount) { this.entryCount = entryCount; }
    }

    public static class CacheEntry {
        private String key;
        private String value;
        private int ttlSeconds;
        private LocalDateTime createdAt;

        public CacheEntry() {}

        public CacheEntry(String key, String value, int ttlSeconds) {
            this.key = key;
            this.value = value;
            this.ttlSeconds = ttlSeconds;
            this.createdAt = LocalDateTime.now();
        }

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        public int getTtlSeconds() { return ttlSeconds; }
        public void setTtlSeconds(int ttlSeconds) { this.ttlSeconds = ttlSeconds; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public String getCreatedAtFormatted() { return createdAt != null ? createdAt.format(FORMATTER) : null; }
    }

    public static class CachePutRequest {
        private String cacheName;
        private String key;
        private String value;
        private int ttlSeconds = 3600;

        public String getCacheName() { return cacheName; }
        public void setCacheName(String cacheName) { this.cacheName = cacheName; }
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        public int getTtlSeconds() { return ttlSeconds; }
        public void setTtlSeconds(int ttlSeconds) { this.ttlSeconds = ttlSeconds; }
    }

    public static class RedisKeyInfo {
        private String key;
        private String type;
        private long ttl;
        private boolean mock;

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public long getTtl() { return ttl; }
        public void setTtl(long ttl) { this.ttl = ttl; }
        public boolean isMock() { return mock; }
        public void setMock(boolean mock) { this.mock = mock; }
    }

    public static class RedisSetRequest {
        private String key;
        private String value;
        private int ttlSeconds;

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        public int getTtlSeconds() { return ttlSeconds; }
        public void setTtlSeconds(int ttlSeconds) { this.ttlSeconds = ttlSeconds; }
    }

    public static class PubSubPublishRequest {
        private String channel;
        private String message;

        public String getChannel() { return channel; }
        public void setChannel(String channel) { this.channel = channel; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class PubSubMessage {
        private String messageId;
        private String channel;
        private String message;
        private LocalDateTime timestamp;

        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        public String getChannel() { return channel; }
        public void setChannel(String channel) { this.channel = channel; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public String getTimestampFormatted() { return timestamp != null ? timestamp.format(FORMATTER) : null; }
    }
}
