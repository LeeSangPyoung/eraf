package com.eraf.sample.controller.util;

import com.eraf.core.converter.JsonConverter;
import com.eraf.core.crypto.Crypto;
import com.eraf.core.crypto.Hash;
import com.eraf.core.crypto.Password;
import com.eraf.core.masking.Masking;
import com.eraf.core.utils.DateUtils;
import com.eraf.core.utils.IdGenerator;
import com.eraf.core.utils.JsonUtils;
import com.eraf.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 유틸리티 기능 컨트롤러 (#43~#49)
 *
 * #43 암호화/복호화 (AES)
 * #44 해시 생성 (SHA, MD5)
 * #45 비밀번호 암호화 (BCrypt)
 * #46 개인정보 마스킹
 * #47 JSON 변환
 * #48 날짜 포맷 변환
 * #49 ID 생성기
 */
@Slf4j
@Controller
@RequestMapping("/utility")
public class UtilityController {

    private static final String DEFAULT_AES_KEY = "eraf-sample-key-32-chars-long!!"; // 32 bytes for AES-256

    @GetMapping
    public String utilityPage(Model model) {
        return "utility/utility";
    }

    // ===== #43 암호화/복호화 (AES) =====
    @PostMapping("/crypto/encrypt")
    @ResponseBody
    public Map<String, Object> encrypt(@RequestBody CryptoRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String key = request.key() != null && !request.key().isEmpty() ? request.key() : DEFAULT_AES_KEY;
            String encrypted = Crypto.encrypt(request.plainText(), key);

            result.put("success", true);
            result.put("plainText", request.plainText());
            result.put("encrypted", encrypted);
            result.put("algorithm", "AES-256");

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @PostMapping("/crypto/decrypt")
    @ResponseBody
    public Map<String, Object> decrypt(@RequestBody CryptoRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String key = request.key() != null && !request.key().isEmpty() ? request.key() : DEFAULT_AES_KEY;
            String decrypted = Crypto.decrypt(request.encryptedText(), key);

            result.put("success", true);
            result.put("encrypted", request.encryptedText());
            result.put("decrypted", decrypted);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    // ===== #44 해시 생성 =====
    @PostMapping("/hash")
    @ResponseBody
    public Map<String, Object> generateHash(@RequestBody HashRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String input = request.text();

            result.put("success", true);
            result.put("input", input);
            result.put("sha256", Hash.hash(input));
            result.put("sha256Base64", Hash.hashBase64(input));

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    // ===== #45 비밀번호 암호화 (BCrypt) =====
    @PostMapping("/password/encode")
    @ResponseBody
    public Map<String, Object> encodePassword(@RequestBody PasswordRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String encoded = Password.hash(request.password());

            result.put("success", true);
            result.put("plainPassword", request.password());
            result.put("encodedPassword", encoded);
            result.put("algorithm", "BCrypt");

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @PostMapping("/password/verify")
    @ResponseBody
    public Map<String, Object> verifyPassword(@RequestBody PasswordVerifyRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean matches = Password.verify(request.rawPassword(), request.encodedPassword());

            result.put("success", true);
            result.put("rawPassword", request.rawPassword());
            result.put("encodedPassword", request.encodedPassword());
            result.put("matches", matches);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    // ===== #46 개인정보 마스킹 =====
    @PostMapping("/mask")
    @ResponseBody
    public Map<String, Object> maskData(@RequestBody MaskRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String masked;
            String pattern = request.pattern() != null ? request.pattern().toUpperCase() : "NAME";

            switch (pattern) {
                case "NAME" -> masked = Masking.name(request.value());
                case "EMAIL" -> masked = Masking.email(request.value());
                case "PHONE" -> masked = Masking.phone(request.value());
                case "CARD" -> masked = Masking.card(request.value());
                case "SSN" -> masked = Masking.residentNo(request.value());
                case "ADDRESS" -> masked = Masking.address(request.value());
                case "ACCOUNT" -> masked = Masking.account(request.value());
                case "CUSTOM" -> {
                    int start = request.start() != null ? request.start() : 0;
                    int length = request.end() != null ? request.end() - start : request.value().length() - start;
                    masked = Masking.mask(request.value(), start, length);
                }
                default -> masked = Masking.name(request.value());
            }

            result.put("success", true);
            result.put("original", request.value());
            result.put("masked", masked);
            result.put("pattern", pattern);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @GetMapping("/mask/examples")
    @ResponseBody
    public Map<String, Object> maskExamples() {
        Map<String, Object> result = new HashMap<>();

        result.put("examples", List.of(
                Map.of("pattern", "NAME", "original", "홍길동", "masked", Masking.name("홍길동")),
                Map.of("pattern", "EMAIL", "original", "test@example.com", "masked", Masking.email("test@example.com")),
                Map.of("pattern", "PHONE", "original", "010-1234-5678", "masked", Masking.phone("010-1234-5678")),
                Map.of("pattern", "CARD", "original", "1234-5678-9012-3456", "masked", Masking.card("1234567890123456")),
                Map.of("pattern", "SSN", "original", "901231-1234567", "masked", Masking.residentNo("901231-1234567")),
                Map.of("pattern", "ADDRESS", "original", "서울시 강남구 테헤란로 123", "masked", Masking.address("서울시 강남구 테헤란로 123")),
                Map.of("pattern", "ACCOUNT", "original", "123-456-789012", "masked", Masking.account("123456789012"))
        ));

        return result;
    }

    // ===== #47 JSON 변환 =====
    @PostMapping("/json/to-object")
    @ResponseBody
    public Map<String, Object> jsonToObject(@RequestBody JsonConvertRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> parsed = JsonUtils.fromJson(request.json(), Map.class);

            result.put("success", true);
            result.put("json", request.json());
            result.put("object", parsed);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @PostMapping("/json/to-json")
    @ResponseBody
    public Map<String, Object> objectToJson(@RequestBody Map<String, Object> object) {
        Map<String, Object> result = new HashMap<>();
        try {
            String json = JsonUtils.toJson(object);
            String prettyJson = JsonUtils.toPrettyJson(object);

            result.put("success", true);
            result.put("object", object);
            result.put("json", json);
            result.put("prettyJson", prettyJson);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    // ===== #48 날짜 포맷 변환 =====
    @PostMapping("/date/format")
    @ResponseBody
    public Map<String, Object> formatDate(@RequestBody DateFormatRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            LocalDateTime dateTime;

            if (request.date() != null && !request.date().isEmpty()) {
                if (request.inputFormat() != null && !request.inputFormat().isEmpty()) {
                    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(request.inputFormat());
                    if (request.inputFormat().contains("HH") || request.inputFormat().contains("mm")) {
                        dateTime = LocalDateTime.parse(request.date(), inputFormatter);
                    } else {
                        dateTime = LocalDate.parse(request.date(), inputFormatter).atStartOfDay();
                    }
                } else {
                    dateTime = LocalDateTime.parse(request.date());
                }
            } else {
                dateTime = LocalDateTime.now();
            }

            String outputFormat = request.outputFormat() != null ? request.outputFormat() : "yyyy-MM-dd HH:mm:ss";
            String formatted = dateTime.format(DateTimeFormatter.ofPattern(outputFormat));

            result.put("success", true);
            result.put("input", request.date() != null ? request.date() : "now");
            result.put("formatted", formatted);
            result.put("outputFormat", outputFormat);

            // 추가 포맷 예시
            result.put("formats", Map.of(
                    "ISO", dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    "yyyyMMdd", dateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                    "yyyy-MM-dd", dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    "yyyy/MM/dd HH:mm", dateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")),
                    "Korean", DateUtils.format(dateTime, "yyyy년 MM월 dd일 HH시 mm분")
            ));

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @GetMapping("/date/now")
    @ResponseBody
    public Map<String, Object> getCurrentDate() {
        Map<String, Object> result = new HashMap<>();

        LocalDateTime now = LocalDateTime.now();

        result.put("localDateTime", now.toString());
        result.put("epoch", System.currentTimeMillis());
        result.put("formats", Map.of(
                "ISO", now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "yyyyMMdd", now.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                "yyyyMMddHHmmss", now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")),
                "yyyy-MM-dd", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                "yyyy-MM-dd HH:mm:ss", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        ));

        return result;
    }

    // ===== #49 ID 생성기 =====
    @GetMapping("/id/generate")
    @ResponseBody
    public Map<String, Object> generateIds(@RequestParam(defaultValue = "5") int count) {
        Map<String, Object> result = new HashMap<>();

        List<String> uuids = new ArrayList<>();
        List<String> nanoids = new ArrayList<>();
        List<String> ulids = new ArrayList<>();
        List<String> timestampIds = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            uuids.add(IdGenerator.uuid());
            nanoids.add(IdGenerator.nanoid());
            ulids.add(IdGenerator.ulid());
            timestampIds.add(IdGenerator.timestampId());
        }

        result.put("uuid", uuids);
        result.put("nanoid", nanoids);
        result.put("ulid", ulids);
        result.put("timestampId", timestampIds);

        return result;
    }

    @GetMapping("/id/uuid")
    @ResponseBody
    public Map<String, Object> generateUUID() {
        return Map.of("uuid", IdGenerator.uuid());
    }

    @GetMapping("/id/ulid")
    @ResponseBody
    public Map<String, Object> generateUlid() {
        String id = IdGenerator.ulid();
        return Map.of(
                "ulid", id,
                "length", id.length()
        );
    }

    @PostMapping("/id/custom")
    @ResponseBody
    public Map<String, Object> generateCustomId(@RequestBody CustomIdRequest request) {
        Map<String, Object> result = new HashMap<>();

        String prefix = request.prefix() != null ? request.prefix() : "";
        String suffix = request.suffix() != null ? request.suffix() : "";
        int length = request.length() != null ? request.length() : 8;

        String randomPart;
        if ("numeric".equalsIgnoreCase(request.type())) {
            randomPart = generateNumericString(length);
        } else if ("alpha".equalsIgnoreCase(request.type())) {
            randomPart = generateAlphaString(length);
        } else {
            String nanoid = IdGenerator.nanoid(length);
            randomPart = nanoid.substring(0, Math.min(length, nanoid.length()));
        }

        String customId = prefix + randomPart + suffix;

        result.put("customId", customId);
        result.put("prefix", prefix);
        result.put("suffix", suffix);
        result.put("type", request.type());
        result.put("length", length);

        return result;
    }

    private String generateNumericString(int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String generateAlphaString(int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // ===== Request DTOs =====
    record CryptoRequest(String plainText, String encryptedText, String key) {}
    record HashRequest(String text) {}
    record PasswordRequest(String password) {}
    record PasswordVerifyRequest(String rawPassword, String encodedPassword) {}
    record MaskRequest(String value, String pattern, Integer start, Integer end) {}
    record JsonConvertRequest(String json) {}
    record DateFormatRequest(String date, String inputFormat, String outputFormat) {}
    record CustomIdRequest(String prefix, String suffix, String type, Integer length) {}
}
