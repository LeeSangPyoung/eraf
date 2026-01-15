package com.eraf.starter.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ERAF 알림 설정
 */
@ConfigurationProperties(prefix = "eraf.notification")
public class ErafNotificationProperties {

    /**
     * 이메일 설정
     */
    private Email email = new Email();

    /**
     * SMS 설정
     */
    private Sms sms = new Sms();

    /**
     * 푸시 설정
     */
    private Push push = new Push();

    public static class Email {
        /**
         * 활성화 여부
         */
        private boolean enabled = true;

        /**
         * 발신자 주소
         */
        private String from;

        /**
         * 발신자 이름
         */
        private String fromName;

        /**
         * 프로바이더 (smtp, ses)
         */
        private EmailProvider provider = EmailProvider.SMTP;

        public enum EmailProvider {
            SMTP, SES
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getFromName() {
            return fromName;
        }

        public void setFromName(String fromName) {
            this.fromName = fromName;
        }

        public EmailProvider getProvider() {
            return provider;
        }

        public void setProvider(EmailProvider provider) {
            this.provider = provider;
        }
    }

    public static class Sms {
        /**
         * 활성화 여부
         */
        private boolean enabled = false;

        /**
         * 발신 번호
         */
        private String from;

        /**
         * 프로바이더 (twilio, naver, nhn, aws-sns, custom)
         */
        private SmsProvider provider = SmsProvider.TWILIO;

        /**
         * Twilio 설정
         */
        private Twilio twilio = new Twilio();

        /**
         * Naver Cloud 설정
         */
        private Naver naver = new Naver();

        /**
         * NHN Cloud 설정
         */
        private Nhn nhn = new Nhn();

        /**
         * Custom API 설정
         */
        private Custom custom = new Custom();

        public enum SmsProvider {
            TWILIO, NAVER, NHN, AWS_SNS, CUSTOM
        }

        public static class Twilio {
            private String accountSid;
            private String authToken;

            public String getAccountSid() { return accountSid; }
            public void setAccountSid(String accountSid) { this.accountSid = accountSid; }
            public String getAuthToken() { return authToken; }
            public void setAuthToken(String authToken) { this.authToken = authToken; }
        }

        public static class Naver {
            private String serviceId;
            private String accessKey;
            private String secretKey;

            public String getServiceId() { return serviceId; }
            public void setServiceId(String serviceId) { this.serviceId = serviceId; }
            public String getAccessKey() { return accessKey; }
            public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
            public String getSecretKey() { return secretKey; }
            public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
        }

        public static class Nhn {
            private String appKey;
            private String secretKey;

            public String getAppKey() { return appKey; }
            public void setAppKey(String appKey) { this.appKey = appKey; }
            public String getSecretKey() { return secretKey; }
            public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
        }

        public static class Custom {
            private String apiUrl;
            private String apiKey;
            private String apiKeyHeader = "X-Api-Key";

            public String getApiUrl() { return apiUrl; }
            public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
            public String getApiKey() { return apiKey; }
            public void setApiKey(String apiKey) { this.apiKey = apiKey; }
            public String getApiKeyHeader() { return apiKeyHeader; }
            public void setApiKeyHeader(String apiKeyHeader) { this.apiKeyHeader = apiKeyHeader; }
        }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }
        public SmsProvider getProvider() { return provider; }
        public void setProvider(SmsProvider provider) { this.provider = provider; }
        public Twilio getTwilio() { return twilio; }
        public void setTwilio(Twilio twilio) { this.twilio = twilio; }
        public Naver getNaver() { return naver; }
        public void setNaver(Naver naver) { this.naver = naver; }
        public Nhn getNhn() { return nhn; }
        public void setNhn(Nhn nhn) { this.nhn = nhn; }
        public Custom getCustom() { return custom; }
        public void setCustom(Custom custom) { this.custom = custom; }
    }

    public static class Push {
        /**
         * 활성화 여부
         */
        private boolean enabled = false;

        /**
         * FCM 설정
         */
        private Fcm fcm = new Fcm();

        /**
         * APNs 설정
         */
        private Apns apns = new Apns();

        public static class Fcm {
            /**
             * 활성화 여부
             */
            private boolean enabled = false;

            /**
             * 서비스 계정 JSON 경로
             */
            private String credentialsPath;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public String getCredentialsPath() {
                return credentialsPath;
            }

            public void setCredentialsPath(String credentialsPath) {
                this.credentialsPath = credentialsPath;
            }
        }

        public static class Apns {
            /**
             * 활성화 여부
             */
            private boolean enabled = false;

            /**
             * 인증서 경로
             */
            private String certificatePath;

            /**
             * 인증서 비밀번호
             */
            private String certificatePassword;

            /**
             * 프로덕션 환경 여부
             */
            private boolean production = false;

            /**
             * 앱 Bundle ID
             */
            private String bundleId;

            public boolean isEnabled() { return enabled; }
            public void setEnabled(boolean enabled) { this.enabled = enabled; }
            public String getCertificatePath() { return certificatePath; }
            public void setCertificatePath(String certificatePath) { this.certificatePath = certificatePath; }
            public String getCertificatePassword() { return certificatePassword; }
            public void setCertificatePassword(String certificatePassword) { this.certificatePassword = certificatePassword; }
            public boolean isProduction() { return production; }
            public void setProduction(boolean production) { this.production = production; }
            public String getBundleId() { return bundleId; }
            public void setBundleId(String bundleId) { this.bundleId = bundleId; }
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Fcm getFcm() {
            return fcm;
        }

        public void setFcm(Fcm fcm) {
            this.fcm = fcm;
        }

        public Apns getApns() {
            return apns;
        }

        public void setApns(Apns apns) {
            this.apns = apns;
        }
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public Sms getSms() {
        return sms;
    }

    public void setSms(Sms sms) {
        this.sms = sms;
    }

    public Push getPush() {
        return push;
    }

    public void setPush(Push push) {
        this.push = push;
    }
}
