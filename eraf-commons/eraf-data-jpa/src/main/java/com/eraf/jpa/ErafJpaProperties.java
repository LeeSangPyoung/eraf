package com.eraf.jpa;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ERAF JPA Configuration Properties
 */
@ConfigurationProperties(prefix = "eraf.jpa")
public class ErafJpaProperties {

    /**
     * Enable JPA auditing
     */
    private boolean auditingEnabled = true;

    /**
     * Enable open-in-view
     */
    private boolean openInView = false;

    /**
     * Show SQL in logs
     */
    private boolean showSql = false;

    /**
     * Format SQL in logs
     */
    private boolean formatSql = true;

    /**
     * Enable code repository
     */
    private boolean codeRepositoryEnabled = true;

    /**
     * Enable audit log
     */
    private boolean auditLogEnabled = true;

    /**
     * Enable optimistic retry aspect
     */
    private boolean optimisticRetryEnabled = true;

    /**
     * Enable Hibernate Envers for entity history
     */
    private boolean enversEnabled = false;

    /**
     * Multi-tenancy 설정
     */
    private MultiTenancy multiTenancy = new MultiTenancy();

    /**
     * Flyway 마이그레이션 설정
     */
    private Flyway flyway = new Flyway();

    /**
     * Audit Retention 설정
     */
    private AuditRetention auditRetention = new AuditRetention();

    /**
     * DataSource 라우팅 설정
     */
    private DataSourceRoutingConfig datasourceRouting = new DataSourceRoutingConfig();

    public boolean isAuditingEnabled() {
        return auditingEnabled;
    }

    public void setAuditingEnabled(boolean auditingEnabled) {
        this.auditingEnabled = auditingEnabled;
    }

    public boolean isOpenInView() {
        return openInView;
    }

    public void setOpenInView(boolean openInView) {
        this.openInView = openInView;
    }

    public boolean isShowSql() {
        return showSql;
    }

    public void setShowSql(boolean showSql) {
        this.showSql = showSql;
    }

    public boolean isFormatSql() {
        return formatSql;
    }

    public void setFormatSql(boolean formatSql) {
        this.formatSql = formatSql;
    }

    public boolean isCodeRepositoryEnabled() {
        return codeRepositoryEnabled;
    }

    public void setCodeRepositoryEnabled(boolean codeRepositoryEnabled) {
        this.codeRepositoryEnabled = codeRepositoryEnabled;
    }

    public boolean isAuditLogEnabled() {
        return auditLogEnabled;
    }

    public void setAuditLogEnabled(boolean auditLogEnabled) {
        this.auditLogEnabled = auditLogEnabled;
    }

    public boolean isOptimisticRetryEnabled() {
        return optimisticRetryEnabled;
    }

    public void setOptimisticRetryEnabled(boolean optimisticRetryEnabled) {
        this.optimisticRetryEnabled = optimisticRetryEnabled;
    }

    public boolean isEnversEnabled() {
        return enversEnabled;
    }

    public void setEnversEnabled(boolean enversEnabled) {
        this.enversEnabled = enversEnabled;
    }

    public MultiTenancy getMultiTenancy() {
        return multiTenancy;
    }

    public void setMultiTenancy(MultiTenancy multiTenancy) {
        this.multiTenancy = multiTenancy;
    }

    public Flyway getFlyway() {
        return flyway;
    }

    public void setFlyway(Flyway flyway) {
        this.flyway = flyway;
    }

    public AuditRetention getAuditRetention() {
        return auditRetention;
    }

    public void setAuditRetention(AuditRetention auditRetention) {
        this.auditRetention = auditRetention;
    }

    public DataSourceRoutingConfig getDatasourceRouting() {
        return datasourceRouting;
    }

    public void setDatasourceRouting(DataSourceRoutingConfig datasourceRouting) {
        this.datasourceRouting = datasourceRouting;
    }

    /**
     * DataSource 라우팅 설정
     */
    public static class DataSourceRoutingConfig {

        /**
         * DataSource 라우팅 활성화 여부
         */
        private boolean enabled = false;

        /**
         * Primary (쓰기) DataSource 키 (@DataSourceRouting 값)
         */
        private String primaryKey = "primary";

        /**
         * ReadOnly (읽기/복제) DataSource 키 (@DataSourceRouting 값)
         */
        private String readOnlyKey = "readOnly";

        /**
         * Replica DataSource 설정
         * 이 URL이 설정되면 멀티 DataSource가 자동 구성됩니다.
         */
        private ReplicaDataSourceConfig replica = new ReplicaDataSourceConfig();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getPrimaryKey() {
            return primaryKey;
        }

        public void setPrimaryKey(String primaryKey) {
            this.primaryKey = primaryKey;
        }

        public String getReadOnlyKey() {
            return readOnlyKey;
        }

        public void setReadOnlyKey(String readOnlyKey) {
            this.readOnlyKey = readOnlyKey;
        }

        public ReplicaDataSourceConfig getReplica() {
            return replica;
        }

        public void setReplica(ReplicaDataSourceConfig replica) {
            this.replica = replica;
        }

        /**
         * Replica DataSource 연결 설정
         *
         * <p>설정 예시:</p>
         * <pre>
         * eraf:
         *   jpa:
         *     datasource-routing:
         *       enabled: true
         *       replica:
         *         url: jdbc:postgresql://replica-host:5432/mydb
         *         username: user
         *         password: ${REPLICA_DB_PASSWORD}
         *         hikari:
         *           maximum-pool-size: 10
         *           pool-name: eraf-replica-pool
         * </pre>
         */
        public static class ReplicaDataSourceConfig {
            private String url;
            private String username;
            private String password;
            private String driverClassName;

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getUsername() {
                return username;
            }

            public void setUsername(String username) {
                this.username = username;
            }

            public String getPassword() {
                return password;
            }

            public void setPassword(String password) {
                this.password = password;
            }

            public String getDriverClassName() {
                return driverClassName;
            }

            public void setDriverClassName(String driverClassName) {
                this.driverClassName = driverClassName;
            }
        }
    }

    /**
     * Audit Retention 설정
     */
    public static class AuditRetention {

        /**
         * Retention 정책 활성화
         */
        private boolean enabled = true;

        /**
         * 보관 일수 (기본: 365일 = 1년)
         */
        private int retentionDays = 365;

        /**
         * 영구 삭제 활성화
         */
        private boolean hardDeleteEnabled = true;

        /**
         * 영구 삭제 기준 일수 (Soft Delete 후, 기본: 730일 = 2년)
         */
        private int hardDeleteAfterDays = 730;

        /**
         * Soft Delete Cron 표현식 (기본: 매일 02:00)
         */
        private String cron = "0 0 2 * * ?";

        /**
         * Hard Delete Cron 표현식 (기본: 매주 일요일 03:00)
         */
        private String hardDeleteCron = "0 0 3 * * SUN";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getRetentionDays() {
            return retentionDays;
        }

        public void setRetentionDays(int retentionDays) {
            this.retentionDays = retentionDays;
        }

        public boolean isHardDeleteEnabled() {
            return hardDeleteEnabled;
        }

        public void setHardDeleteEnabled(boolean hardDeleteEnabled) {
            this.hardDeleteEnabled = hardDeleteEnabled;
        }

        public int getHardDeleteAfterDays() {
            return hardDeleteAfterDays;
        }

        public void setHardDeleteAfterDays(int hardDeleteAfterDays) {
            this.hardDeleteAfterDays = hardDeleteAfterDays;
        }

        public String getCron() {
            return cron;
        }

        public void setCron(String cron) {
            this.cron = cron;
        }

        public String getHardDeleteCron() {
            return hardDeleteCron;
        }

        public void setHardDeleteCron(String hardDeleteCron) {
            this.hardDeleteCron = hardDeleteCron;
        }
    }

    /**
     * Flyway 마이그레이션 설정
     */
    public static class Flyway {

        /**
         * Flyway 활성화
         */
        private boolean enabled = true;

        /**
         * 마이그레이션 스크립트 위치
         */
        private String location = "classpath:db/migration";

        /**
         * Baseline 버전
         */
        private String baselineVersion = "1.0.0";

        /**
         * Baseline on migrate 활성화
         */
        private boolean baselineOnMigrate = true;

        /**
         * 검증 활성화
         */
        private boolean validateOnMigrate = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getBaselineVersion() {
            return baselineVersion;
        }

        public void setBaselineVersion(String baselineVersion) {
            this.baselineVersion = baselineVersion;
        }

        public boolean isBaselineOnMigrate() {
            return baselineOnMigrate;
        }

        public void setBaselineOnMigrate(boolean baselineOnMigrate) {
            this.baselineOnMigrate = baselineOnMigrate;
        }

        public boolean isValidateOnMigrate() {
            return validateOnMigrate;
        }

        public void setValidateOnMigrate(boolean validateOnMigrate) {
            this.validateOnMigrate = validateOnMigrate;
        }
    }

    /**
     * Multi-tenancy 설정
     */
    public static class MultiTenancy {

        /**
         * Multi-tenancy 활성화
         */
        private boolean enabled = false;

        /**
         * 테넌트 ID 헤더 이름
         */
        private String headerName = "X-Tenant-ID";

        /**
         * 기본 테넌트 ID
         */
        private String defaultTenantId;

        /**
         * 테넌트 ID 필수 여부
         */
        private boolean required = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getHeaderName() {
            return headerName;
        }

        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }

        public String getDefaultTenantId() {
            return defaultTenantId;
        }

        public void setDefaultTenantId(String defaultTenantId) {
            this.defaultTenantId = defaultTenantId;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }
    }
}
