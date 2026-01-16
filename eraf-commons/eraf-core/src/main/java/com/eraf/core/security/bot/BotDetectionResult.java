package com.eraf.core.security.bot;

/**
 * 봇 탐지 결과
 */
public class BotDetectionResult {

    private final boolean bot;
    private final BotType botType;
    private final String botName;
    private final double confidence;
    private final DetectionMethod detectionMethod;
    private final boolean allowed;

    private BotDetectionResult(boolean bot, BotType botType, String botName,
                               double confidence, DetectionMethod detectionMethod, boolean allowed) {
        this.bot = bot;
        this.botType = botType;
        this.botName = botName;
        this.confidence = confidence;
        this.detectionMethod = detectionMethod;
        this.allowed = allowed;
    }

    /**
     * 봇이 아닌 경우
     */
    public static BotDetectionResult notBot() {
        return new BotDetectionResult(false, null, null, 0.0, null, true);
    }

    /**
     * 허용된 봇으로 탐지
     */
    public static BotDetectionResult allowedBot(BotType type, String name,
                                                 double confidence, DetectionMethod method) {
        return new BotDetectionResult(true, type, name, confidence, method, true);
    }

    /**
     * 차단된 봇으로 탐지
     */
    public static BotDetectionResult blockedBot(BotType type, String name,
                                                 double confidence, DetectionMethod method) {
        return new BotDetectionResult(true, type, name, confidence, method, false);
    }

    /**
     * Builder 생성
     */
    public static Builder builder() {
        return new Builder();
    }

    // Getters

    public boolean isBot() {
        return bot;
    }

    public BotType getBotType() {
        return botType;
    }

    public String getBotName() {
        return botName;
    }

    public double getConfidence() {
        return confidence;
    }

    public DetectionMethod getDetectionMethod() {
        return detectionMethod;
    }

    public boolean isAllowed() {
        return allowed;
    }

    /**
     * Builder
     */
    public static class Builder {
        private boolean bot;
        private BotType botType;
        private String botName;
        private double confidence;
        private DetectionMethod detectionMethod;
        private boolean allowed = true;

        public Builder bot(boolean bot) {
            this.bot = bot;
            return this;
        }

        public Builder botType(BotType botType) {
            this.botType = botType;
            return this;
        }

        public Builder botName(String botName) {
            this.botName = botName;
            return this;
        }

        public Builder confidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder detectionMethod(DetectionMethod detectionMethod) {
            this.detectionMethod = detectionMethod;
            return this;
        }

        public Builder allowed(boolean allowed) {
            this.allowed = allowed;
            return this;
        }

        public BotDetectionResult build() {
            return new BotDetectionResult(bot, botType, botName, confidence, detectionMethod, allowed);
        }
    }
}
