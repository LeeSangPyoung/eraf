package com.eraf.starter.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ERAF Security Configuration Properties
 */
@ConfigurationProperties(prefix = "eraf.security")
public class ErafSecurityProperties {

    /**
     * Disable CSRF protection (use with caution)
     */
    private boolean disableCsrf = false;

    /**
     * Disable X-Frame-Options (use with caution)
     */
    private boolean disableFrameOptions = false;

    /**
     * URL patterns to permit without authentication
     */
    private String[] permitAllPatterns = {
            "/actuator/health",
            "/actuator/info",
            "/error",
            "/favicon.ico"
    };

    /**
     * Enable form login
     */
    private boolean formLoginEnabled = true;

    /**
     * Login page URL
     */
    private String loginPage = "/login";

    /**
     * Login success URL
     */
    private String loginSuccessUrl = "/";

    /**
     * Logout URL
     */
    private String logoutUrl = "/logout";

    /**
     * Logout success URL
     */
    private String logoutSuccessUrl = "/login?logout";

    public boolean isDisableCsrf() {
        return disableCsrf;
    }

    public void setDisableCsrf(boolean disableCsrf) {
        this.disableCsrf = disableCsrf;
    }

    public boolean isDisableFrameOptions() {
        return disableFrameOptions;
    }

    public void setDisableFrameOptions(boolean disableFrameOptions) {
        this.disableFrameOptions = disableFrameOptions;
    }

    public String[] getPermitAllPatterns() {
        return permitAllPatterns;
    }

    public void setPermitAllPatterns(String[] permitAllPatterns) {
        this.permitAllPatterns = permitAllPatterns;
    }

    public boolean isFormLoginEnabled() {
        return formLoginEnabled;
    }

    public void setFormLoginEnabled(boolean formLoginEnabled) {
        this.formLoginEnabled = formLoginEnabled;
    }

    public String getLoginPage() {
        return loginPage;
    }

    public void setLoginPage(String loginPage) {
        this.loginPage = loginPage;
    }

    public String getLoginSuccessUrl() {
        return loginSuccessUrl;
    }

    public void setLoginSuccessUrl(String loginSuccessUrl) {
        this.loginSuccessUrl = loginSuccessUrl;
    }

    public String getLogoutUrl() {
        return logoutUrl;
    }

    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }

    public String getLogoutSuccessUrl() {
        return logoutSuccessUrl;
    }

    public void setLogoutSuccessUrl(String logoutSuccessUrl) {
        this.logoutSuccessUrl = logoutSuccessUrl;
    }
}
