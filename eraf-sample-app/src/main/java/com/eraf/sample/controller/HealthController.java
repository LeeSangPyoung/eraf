package com.eraf.sample.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class HealthController {

    /**
     * 헬스 체크
     * GET /health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now(),
            "application", "eraf-sample-app"
        ));
    }

    /**
     * 공개 엔드포인트
     * GET /public/info
     */
    @GetMapping("/public/info")
    public ResponseEntity<Map<String, Object>> publicInfo() {
        return ResponseEntity.ok(Map.of(
            "name", "ERAF Sample Application",
            "version", "1.0.0",
            "description", "eraf-commons 기반 샘플 애플리케이션"
        ));
    }
}
