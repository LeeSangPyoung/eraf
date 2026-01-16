package com.eraf.starter.swagger;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ERAF Swagger Configuration Properties
 */
@ConfigurationProperties(prefix = "eraf.swagger")
public class ErafSwaggerProperties {

    /**
     * Swagger 활성화 여부
     */
    private boolean enabled = true;

    /**
     * API 정보
     */
    private ApiInfo apiInfo = new ApiInfo();

    /**
     * 보안 설정
     */
    private Security security = new Security();

    /**
     * 그룹 설정
     */
    private Group group = new Group();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ApiInfo getApiInfo() {
        return apiInfo;
    }

    public void setApiInfo(ApiInfo apiInfo) {
        this.apiInfo = apiInfo;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    /**
     * API 정보 설정
     */
    public static class ApiInfo {
        private String title = "ERAF API Documentation";
        private String description = "API Documentation powered by ERAF Commons";
        private String version = "1.0.0";
        private String termsOfService = "";
        private Contact contact = new Contact();
        private License license = new License();

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getTermsOfService() {
            return termsOfService;
        }

        public void setTermsOfService(String termsOfService) {
            this.termsOfService = termsOfService;
        }

        public Contact getContact() {
            return contact;
        }

        public void setContact(Contact contact) {
            this.contact = contact;
        }

        public License getLicense() {
            return license;
        }

        public void setLicense(License license) {
            this.license = license;
        }
    }

    /**
     * 연락처 정보
     */
    public static class Contact {
        private String name = "";
        private String url = "";
        private String email = "";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    /**
     * 라이센스 정보
     */
    public static class License {
        private String name = "Apache 2.0";
        private String url = "https://www.apache.org/licenses/LICENSE-2.0";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    /**
     * 보안 설정
     */
    public static class Security {
        private boolean enabled = true;
        private String schemeName = "bearerAuth";
        private String scheme = "bearer";
        private String bearerFormat = "JWT";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getSchemeName() {
            return schemeName;
        }

        public void setSchemeName(String schemeName) {
            this.schemeName = schemeName;
        }

        public String getScheme() {
            return scheme;
        }

        public void setScheme(String scheme) {
            this.scheme = scheme;
        }

        public String getBearerFormat() {
            return bearerFormat;
        }

        public void setBearerFormat(String bearerFormat) {
            this.bearerFormat = bearerFormat;
        }
    }

    /**
     * API 그룹 설정
     */
    public static class Group {
        private String defaultGroup = "all";
        private String[] packagesToScan = {};
        private String[] pathsToMatch = {"/**"};
        private String[] pathsToExclude = {"/actuator/**"};

        public String getDefaultGroup() {
            return defaultGroup;
        }

        public void setDefaultGroup(String defaultGroup) {
            this.defaultGroup = defaultGroup;
        }

        public String[] getPackagesToScan() {
            return packagesToScan;
        }

        public void setPackagesToScan(String[] packagesToScan) {
            this.packagesToScan = packagesToScan;
        }

        public String[] getPathsToMatch() {
            return pathsToMatch;
        }

        public void setPathsToMatch(String[] pathsToMatch) {
            this.pathsToMatch = pathsToMatch;
        }

        public String[] getPathsToExclude() {
            return pathsToExclude;
        }

        public void setPathsToExclude(String[] pathsToExclude) {
            this.pathsToExclude = pathsToExclude;
        }
    }
}
