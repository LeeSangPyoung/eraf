package com.eraf.sample.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SessionInfo {
    private boolean valid;
    private String sessionId;
    private String username;
    private LocalDateTime createdAt;
    private LocalDateTime lastAccessedAt;
    private int maxInactiveIntervalSeconds;
}
