package com.eraf.sample.controller;

import com.eraf.core.crypto.Crypto;
import com.eraf.core.crypto.Hash;
import com.eraf.core.crypto.Password;
import com.eraf.core.masking.Masking;
import com.eraf.core.response.ApiResponse;
import com.eraf.core.sequence.Reset;
import com.eraf.core.sequence.SequenceGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * eraf-core 유틸리티 데모 컨트롤러
 * 암호화, 마스킹, 해시, 채번 등 다양한 기능 시연
 */
@Slf4j
@RestController
@RequestMapping("/api/demo")
@Tag(name = "Utility Demo", description = "eraf-core 유틸리티 기능 데모")
public class UtilDemoController {

    // ==================== 마스킹 ====================

    @Operation(summary = "마스킹 데모", description = "개인정보 마스킹 기능")
    @GetMapping("/masking")
    public ResponseEntity<ApiResponse<Map<String, Object>>> maskingDemo(
            @RequestParam(defaultValue = "홍길동") String name,
            @RequestParam(defaultValue = "01012345678") String phone,
            @RequestParam(defaultValue = "test@gmail.com") String email,
            @RequestParam(defaultValue = "900101-1234567") String residentNo,
            @RequestParam(defaultValue = "1234567890123456") String cardNo) {

        Map<String, Object> result = new LinkedHashMap<>();

        // 원본 데이터
        Map<String, String> original = new LinkedHashMap<>();
        original.put("이름", name);
        original.put("전화번호", phone);
        original.put("이메일", email);
        original.put("주민등록번호", residentNo);
        original.put("카드번호", cardNo);
        result.put("원본", original);

        // 마스킹된 데이터
        Map<String, String> masked = new LinkedHashMap<>();
        masked.put("이름", Masking.name(name));
        masked.put("전화번호", Masking.phone(phone));
        masked.put("이메일", Masking.email(email));
        masked.put("주민등록번호", Masking.residentNo(residentNo));
        masked.put("카드번호", Masking.card(cardNo));
        result.put("마스킹", masked);

        log.info("Masking demo executed");
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ==================== 암호화 ====================

    @Operation(summary = "AES 암호화/복호화 데모")
    @PostMapping("/encrypt")
    public ResponseEntity<ApiResponse<Map<String, String>>> encryptDemo(
            @RequestParam String text,
            @RequestParam(defaultValue = "my-secret-key-1234567890123456") String secretKey) {

        Map<String, String> result = new LinkedHashMap<>();
        result.put("원본", text);

        // AES-256-GCM 암호화
        String encrypted = Crypto.encrypt(text, secretKey);
        result.put("암호화", encrypted);

        // AES 복호화
        String decrypted = Crypto.decrypt(encrypted, secretKey);
        result.put("복호화", decrypted);

        log.info("Encryption demo: {} -> [encrypted] -> {}", text, decrypted);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ==================== 해시 ====================

    @Operation(summary = "해시 함수 데모", description = "SHA-256 해시")
    @GetMapping("/hash")
    public ResponseEntity<ApiResponse<Map<String, String>>> hashDemo(
            @RequestParam String text) {

        Map<String, String> result = new LinkedHashMap<>();
        result.put("원본", text);
        result.put("SHA-256 (Hex)", Hash.hash(text));
        result.put("SHA-256 (Base64)", Hash.hashBase64(text));
        result.put("검증결과", String.valueOf(Hash.verify(text, Hash.hash(text))));

        log.info("Hash demo for text: {}", text);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ==================== 비밀번호 ====================

    @Operation(summary = "비밀번호 해시 데모", description = "bcrypt 해시 생성 및 검증")
    @PostMapping("/password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> passwordDemo(
            @RequestParam String password) {

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("원본", password);

        // bcrypt 해시 생성
        String hashed = Password.hash(password);
        result.put("bcrypt_해시", hashed);

        // 검증
        boolean verified = Password.verify(password, hashed);
        result.put("검증결과", verified);

        // 잘못된 비밀번호로 검증
        boolean wrongVerified = Password.verify("wrong-password", hashed);
        result.put("잘못된_비밀번호_검증", wrongVerified);

        // 재해시 필요 여부
        boolean needsRehash = Password.needsRehash(hashed);
        result.put("재해시_필요", needsRehash);

        log.info("Password demo executed");
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ==================== 채번 ====================

    @Operation(summary = "채번 데모", description = "시퀀스 번호 생성")
    @GetMapping("/sequence")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sequenceDemo(
            @RequestParam(defaultValue = "ORDER") String prefix) {

        Map<String, Object> result = new LinkedHashMap<>();

        // 일별 채번 (ORDER-20260117-0001)
        String dailySeq1 = SequenceGenerator.next("daily-order", prefix, Reset.DAILY, 4);
        String dailySeq2 = SequenceGenerator.next("daily-order", prefix, Reset.DAILY, 4);
        String dailySeq3 = SequenceGenerator.next("daily-order", prefix, Reset.DAILY, 4);

        Map<String, String> daily = new LinkedHashMap<>();
        daily.put("1번", dailySeq1);
        daily.put("2번", dailySeq2);
        daily.put("3번", dailySeq3);
        result.put("일별채번", daily);

        // 월별 채번 (INV-202601-0001)
        String monthlySeq = SequenceGenerator.next("monthly-invoice", "INV", Reset.MONTHLY, 4);
        result.put("월별채번", monthlySeq);

        // 연속 채번 (NO-0001)
        String neverSeq = SequenceGenerator.next("never-reset", "NO", Reset.NEVER, 4);
        result.put("연속채번", neverSeq);

        log.info("Sequence demo executed");
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ==================== 추가 마스킹 ====================

    @Operation(summary = "추가 마스킹 데모", description = "IP, 계좌번호, 주소 마스킹")
    @GetMapping("/masking/extra")
    public ResponseEntity<ApiResponse<Map<String, Object>>> extraMaskingDemo(
            @RequestParam(defaultValue = "192.168.1.100") String ip,
            @RequestParam(defaultValue = "1234567890") String accountNo,
            @RequestParam(defaultValue = "서울시 강남구 역삼동 123-45") String address,
            @RequestParam(defaultValue = "12가1234") String carNo) {

        Map<String, Object> result = new LinkedHashMap<>();

        // 원본
        Map<String, String> original = new LinkedHashMap<>();
        original.put("IP주소", ip);
        original.put("계좌번호", accountNo);
        original.put("주소", address);
        original.put("차량번호", carNo);
        result.put("원본", original);

        // 마스킹
        Map<String, String> masked = new LinkedHashMap<>();
        masked.put("IP주소", Masking.ip(ip));
        masked.put("계좌번호", Masking.account(accountNo));
        masked.put("주소", Masking.address(address));
        masked.put("차량번호", Masking.carNumber(carNo));
        result.put("마스킹", masked);

        log.info("Extra masking demo executed");
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ==================== 종합 데모 ====================

    @Operation(summary = "eraf-core 기능 종합 데모")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> allDemo() {

        Map<String, Object> result = new LinkedHashMap<>();

        // 1. 마스킹
        Map<String, String> masking = new LinkedHashMap<>();
        masking.put("이름", Masking.name("홍길동"));
        masking.put("전화번호", Masking.phone("01012345678"));
        masking.put("이메일", Masking.email("test@example.com"));
        masking.put("카드번호", Masking.card("1234567890123456"));
        masking.put("주민등록번호", Masking.residentNo("900101-1234567"));
        result.put("마스킹", masking);

        // 2. 암호화
        Map<String, String> crypto = new LinkedHashMap<>();
        String secretKey = "demo-secret-key-1234567890123456";
        String original = "민감한 데이터";
        String encrypted = Crypto.encrypt(original, secretKey);
        crypto.put("원본", original);
        crypto.put("암호화", encrypted);
        crypto.put("복호화", Crypto.decrypt(encrypted, secretKey));
        result.put("AES_암호화", crypto);

        // 3. 해시
        Map<String, String> hash = new LinkedHashMap<>();
        hash.put("원본", "hello");
        hash.put("SHA-256", Hash.hash("hello"));
        result.put("해시", hash);

        // 4. 비밀번호
        Map<String, Object> password = new LinkedHashMap<>();
        String pwHash = Password.hash("mypassword");
        password.put("bcrypt_해시", pwHash);
        password.put("검증", Password.verify("mypassword", pwHash));
        result.put("비밀번호", password);

        // 5. 채번
        Map<String, String> sequence = new LinkedHashMap<>();
        sequence.put("주문번호", SequenceGenerator.next("demo-order", "ORD", Reset.DAILY, 4));
        sequence.put("송장번호", SequenceGenerator.next("demo-invoice", "INV", Reset.MONTHLY, 5));
        result.put("채번", sequence);

        // 6. 현재 시간
        Map<String, String> datetime = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();
        datetime.put("현재시간", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        datetime.put("UUID", UUID.randomUUID().toString());
        result.put("기타", datetime);

        log.info("All demo executed");
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
