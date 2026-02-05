package com.eraf.security.jwt;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * ERAF 확장 UserDetails
 * 사용자 ID 등 추가 정보 포함
 */
public class ErafUserDetails extends User {

    private final String userId;
    private final String displayName;
    private final String email;

    public ErafUserDetails(String username, String password,
                           Collection<? extends GrantedAuthority> authorities,
                           String userId) {
        super(username, password, authorities);
        this.userId = userId;
        this.displayName = null;
        this.email = null;
    }

    public ErafUserDetails(String username, String password,
                           Collection<? extends GrantedAuthority> authorities,
                           String userId, String displayName, String email) {
        super(username, password, authorities);
        this.userId = userId;
        this.displayName = displayName;
        this.email = email;
    }

    public ErafUserDetails(String username, String password, boolean enabled,
                           boolean accountNonExpired, boolean credentialsNonExpired,
                           boolean accountNonLocked,
                           Collection<? extends GrantedAuthority> authorities,
                           String userId, String displayName, String email) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.userId = userId;
        this.displayName = displayName;
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    /**
     * Builder 패턴 지원
     */
    public static Builder erafBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String username;
        private String password = "";
        private Collection<? extends GrantedAuthority> authorities;
        private String userId;
        private String displayName;
        private String email;
        private boolean enabled = true;
        private boolean accountNonExpired = true;
        private boolean credentialsNonExpired = true;
        private boolean accountNonLocked = true;

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder authorities(Collection<? extends GrantedAuthority> authorities) {
            this.authorities = authorities;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder accountNonExpired(boolean accountNonExpired) {
            this.accountNonExpired = accountNonExpired;
            return this;
        }

        public Builder credentialsNonExpired(boolean credentialsNonExpired) {
            this.credentialsNonExpired = credentialsNonExpired;
            return this;
        }

        public Builder accountNonLocked(boolean accountNonLocked) {
            this.accountNonLocked = accountNonLocked;
            return this;
        }

        public ErafUserDetails build() {
            return new ErafUserDetails(
                    username, password, enabled, accountNonExpired,
                    credentialsNonExpired, accountNonLocked, authorities,
                    userId, displayName, email
            );
        }
    }
}
