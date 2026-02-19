package com.eraf.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

/**
 * ERAF HTTP Auto-Configuration
 *
 * <p>HTTP 클라이언트 기능을 자동 구성합니다.</p>
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>HTTP GET/POST/PUT/DELETE/PATCH 요청</li>
 *   <li>파일 업로드/다운로드</li>
 *   <li>타임아웃, 재시도, Interceptor 설정</li>
 *   <li>Bearer Token 인증</li>
 * </ul>
 *
 * <p>사용 예시:</p>
 * <pre>
 * // ErafHttpClient는 Builder 패턴으로 생성하여 사용
 * ErafHttpClient client = ErafHttpClient.create()
 *     .baseUrl("https://api.example.com")
 *     .timeout(Duration.ofSeconds(30))
 *     .bearerToken(token)
 *     .build();
 *
 * // GET 요청
 * String response = client.get("/users");
 *
 * // POST 요청
 * String response = client.post("/users", userDto);
 *
 * // 파일 다운로드
 * client.download("/files/report.pdf", Paths.get("report.pdf"));
 * </pre>
 *
 * <p>Note: ErafHttpClient는 Builder 패턴으로 사용하므로 Bean 주입이 아닌 create()로 생성합니다.</p>
 */
@AutoConfiguration
@ConditionalOnClass(name = "okhttp3.OkHttpClient")
public class ErafCoreHttpAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ErafCoreHttpAutoConfiguration.class);

    public ErafCoreHttpAutoConfiguration() {
        log.info("ERAF HTTP module enabled - HTTP client utilities available");
        log.debug("Use ErafHttpClient.create() to build HTTP client instances");
    }
}
