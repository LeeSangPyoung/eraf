# OAuth2/OIDC 인증

Google, GitHub, Keycloak 등 OAuth2/OIDC 제공자를 통한 인증을 지원합니다.

## 설정 예시

### Google OAuth2
```yaml
eraf:
  security:
    oauth2:
      enabled: true
      providers:
        google:
          issuer-uri: https://accounts.google.com
          authorization-uri: https://accounts.google.com/o/oauth2/v2/auth
          token-uri: https://oauth2.googleapis.com/token
          user-info-uri: https://www.googleapis.com/oauth2/v3/userinfo
          jwk-set-uri: https://www.googleapis.com/oauth2/v3/certs
          user-name-attribute: sub
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: openid,profile,email
```

### Keycloak
```yaml
eraf:
  security:
    oauth2:
      enabled: true
      providers:
        keycloak:
          issuer-uri: http://localhost:8080/realms/myrealm
          authorization-uri: http://localhost:8080/realms/myrealm/protocol/openid-connect/auth
          token-uri: http://localhost:8080/realms/myrealm/protocol/openid-connect/token
          user-info-uri: http://localhost:8080/realms/myrealm/protocol/openid-connect/userinfo
          jwk-set-uri: http://localhost:8080/realms/myrealm/protocol/openid-connect/certs
      client:
        registration:
          keycloak:
            client-id: my-client
            client-secret: secret
            scope: openid,profile
```
