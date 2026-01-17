# ERAF Gateway Feature - OAuth2

Kong-level advanced OAuth2 authentication feature for ERAF API Gateway.

## Overview

This module provides comprehensive OAuth2 2.0 authentication and authorization capabilities for the ERAF API Gateway, following industry standards including RFC 6749 (OAuth 2.0) and RFC 7662 (Token Introspection).

## Features

### 1. OAuth2 Grant Types Support

- **Authorization Code Flow**: Standard OAuth2 flow for web applications
- **Client Credentials Flow**: Machine-to-machine authentication
- **Password Grant**: Resource Owner Password Credentials flow
- **Refresh Token**: Token renewal without re-authentication
- **Implicit Flow**: Legacy support for browser-based applications

### 2. Token Validation Methods

#### Local Validation
- Fast in-memory or database-backed token validation
- No external service dependency
- Configurable token expiration

#### Remote Introspection (RFC 7662)
- Integration with external OAuth2 authorization servers
- Standard introspection endpoint support
- Compatible with Keycloak, Auth0, Okta, etc.

### 3. Scope-Based Access Control

- Fine-grained permission management
- Required scope validation per route
- Hierarchical scope support
- Scope propagation to downstream services

### 4. Security Features

- Secure token generation using `SecureRandom`
- Token revocation support
- PKCE (Proof Key for Code Exchange) support
- Token masking in logs
- WWW-Authenticate header support

## Architecture

### Filter Order
```
FilterOrder.OAUTH2 = HIGHEST + 32
```
Positioned between API_KEY (30) and JWT (35) filters.

### Components

```
eraf-gateway-feature-oauth2/
├── domain/
│   ├── OAuth2Token.java              # Access/Refresh token model
│   ├── OAuth2Client.java             # OAuth2 client application
│   └── OAuth2AuthorizationCode.java  # Authorization code model
├── service/
│   ├── OAuth2Service.java            # Core OAuth2 logic
│   └── TokenIntrospection.java       # RFC 7662 response
├── filter/
│   └── OAuth2Filter.java             # Authentication filter
├── repository/
│   ├── OAuth2TokenRepository.java
│   ├── OAuth2ClientRepository.java
│   └── OAuth2AuthorizationCodeRepository.java
├── config/
│   ├── OAuth2Properties.java         # Configuration properties
│   └── OAuth2AutoConfiguration.java  # Spring Boot auto-config
└── exception/
    ├── OAuth2Exception.java
    ├── InvalidTokenException.java
    └── InsufficientScopeException.java
```

## Configuration

### Basic Configuration

```yaml
eraf:
  gateway:
    oauth2:
      enabled: true
      token-header-name: Authorization
      token-prefix: "Bearer "
      validate-scopes: true
      required-scopes:
        - read
        - write
```

### Local Validation Mode

```yaml
eraf:
  gateway:
    oauth2:
      local-validation: true
      default-access-token-validity-seconds: 3600    # 1 hour
      default-refresh-token-validity-seconds: 2592000 # 30 days
```

### Remote Introspection Mode

```yaml
eraf:
  gateway:
    oauth2:
      local-validation: false
      introspection-endpoint: https://auth.example.com/oauth2/introspect
      introspection-client-id: gateway-client
      introspection-client-secret: secret123
```

### Exclude Patterns

```yaml
eraf:
  gateway:
    oauth2:
      exclude-patterns:
        - /public/**
        - /health
        - /metrics
        - /api/v1/login
```

### Cookie Support

```yaml
eraf:
  gateway:
    oauth2:
      allow-cookie: true
      cookie-name: access_token
```

### Claims Propagation

```yaml
eraf:
  gateway:
    oauth2:
      propagate-claims: true  # Adds X-OAuth2-* headers to requests
```

## Integration Examples

### 1. Keycloak Integration

```yaml
eraf:
  gateway:
    oauth2:
      local-validation: false
      introspection-endpoint: https://keycloak.example.com/realms/master/protocol/openid-connect/token/introspect
      introspection-client-id: api-gateway
      introspection-client-secret: ${KEYCLOAK_CLIENT_SECRET}
      validate-scopes: true
      required-scopes:
        - api:read
        - api:write
```

**Keycloak Setup:**
1. Create a new client in Keycloak admin console
2. Set Access Type to "confidential"
3. Enable "Service Accounts Enabled"
4. Add client scopes for fine-grained permissions
5. Copy client secret to configuration

### 2. Auth0 Integration

```yaml
eraf:
  gateway:
    oauth2:
      local-validation: false
      introspection-endpoint: https://YOUR_DOMAIN.auth0.com/oauth/token/introspection
      introspection-client-id: ${AUTH0_CLIENT_ID}
      introspection-client-secret: ${AUTH0_CLIENT_SECRET}
```

**Auth0 Setup:**
1. Create an API in Auth0 dashboard
2. Create a Machine-to-Machine application
3. Authorize the application to access your API
4. Define custom scopes in API settings
5. Use the client credentials in configuration

### 3. Okta Integration

```yaml
eraf:
  gateway:
    oauth2:
      local-validation: false
      introspection-endpoint: https://YOUR_DOMAIN.okta.com/oauth2/default/v1/introspect
      introspection-client-id: ${OKTA_CLIENT_ID}
      introspection-client-secret: ${OKTA_CLIENT_SECRET}
```

### 4. Local Database Integration

For local validation, implement the repository interfaces:

```java
@Component
public class JpaOAuth2TokenRepository implements OAuth2TokenRepository {

    @Autowired
    private OAuth2TokenJpaRepository jpaRepository;

    @Override
    public Optional<OAuth2Token> findByAccessToken(String accessToken) {
        return jpaRepository.findByAccessToken(accessToken)
            .map(this::toDomain);
    }

    // ... implement other methods
}
```

## Usage Examples

### Client Application Authentication

```bash
# 1. Client Credentials Flow (Machine-to-Machine)
curl -X POST https://auth.example.com/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=YOUR_CLIENT_ID" \
  -d "client_secret=YOUR_CLIENT_SECRET" \
  -d "scope=read write"

# Response:
{
  "access_token": "eyJhbGciOiJSUzI1...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "scope": "read write"
}
```

### Making Authenticated Requests

```bash
# Using Bearer token in Authorization header
curl -X GET https://api.example.com/protected/resource \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1..."

# Using token in cookie (if enabled)
curl -X GET https://api.example.com/protected/resource \
  --cookie "access_token=eyJhbGciOiJSUzI1..."
```

### Token Introspection

```bash
curl -X POST https://api.example.com/oauth2/introspect \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "client_id:client_secret" \
  -d "token=eyJhbGciOiJSUzI1..."

# Response (RFC 7662):
{
  "active": true,
  "scope": "read write",
  "client_id": "client123",
  "username": "user@example.com",
  "token_type": "Bearer",
  "exp": 1735689600,
  "iat": 1735686000,
  "sub": "user123"
}
```

### Refresh Token Flow

```bash
curl -X POST https://auth.example.com/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=refresh_token" \
  -d "refresh_token=YOUR_REFRESH_TOKEN" \
  -d "client_id=YOUR_CLIENT_ID" \
  -d "client_secret=YOUR_CLIENT_SECRET"
```

## Request Attributes

The OAuth2Filter sets the following request attributes for downstream services:

```java
// In your controller or service
@GetMapping("/protected")
public ResponseEntity<?> protectedResource(HttpServletRequest request) {
    OAuth2Token token = (OAuth2Token) request.getAttribute("ERAF_OAUTH2_TOKEN");
    String userId = (String) request.getAttribute("ERAF_OAUTH2_USER_ID");
    String clientId = (String) request.getAttribute("ERAF_OAUTH2_CLIENT_ID");
    List<String> scopes = (List<String>) request.getAttribute("ERAF_OAUTH2_SCOPES");

    // Use authentication information
    return ResponseEntity.ok("Hello, " + userId);
}
```

## Response Headers

When `propagate-claims` is enabled, the following headers are added:

- `X-OAuth2-User-Id`: User ID from token
- `X-OAuth2-Client-Id`: Client ID from token
- `X-OAuth2-Scopes`: Comma-separated list of scopes

## Error Responses

### Missing Token (401)
```json
{
  "code": "OAUTH2_TOKEN_MISSING",
  "message": "OAuth2 토큰이 필요합니다",
  "status": 401
}
```
Response Headers: `WWW-Authenticate: Bearer`

### Invalid Token (401)
```json
{
  "code": "OAUTH2_TOKEN_INVALID",
  "message": "유효하지 않은 OAuth2 토큰입니다",
  "status": 401
}
```
Response Headers: `WWW-Authenticate: Bearer error="invalid_token"`

### Expired Token (401)
```json
{
  "code": "OAUTH2_TOKEN_EXPIRED",
  "message": "만료된 OAuth2 토큰입니다",
  "status": 401
}
```

### Insufficient Scope (403)
```json
{
  "code": "OAUTH2_INSUFFICIENT_SCOPE",
  "message": "권한 범위가 부족합니다",
  "status": 403
}
```
Response Headers: `WWW-Authenticate: Bearer error="insufficient_scope", scope="read write"`

## Programmatic Usage

### Validating Tokens in Code

```java
@Service
@RequiredArgsConstructor
public class MyService {

    private final OAuth2Service oauth2Service;

    public void processRequest(String accessToken) {
        // Validate token
        OAuth2Token token = oauth2Service.validateToken(accessToken);

        // Check specific scope
        if (token.hasScope("admin")) {
            // Admin operation
        }

        // Check multiple scopes
        oauth2Service.validateScope(
            List.of("read", "write"),
            token.getScopes()
        );
    }
}
```

### Generating Tokens

```java
@Service
@RequiredArgsConstructor
public class AuthService {

    private final OAuth2Service oauth2Service;

    public OAuth2Token authenticate(String clientId, String userId) {
        // Generate token with scopes
        OAuth2Token token = oauth2Service.generateToken(
            clientId,
            userId,
            List.of("read", "write", "admin")
        );

        return token;
    }
}
```

### Token Refresh

```java
@Service
@RequiredArgsConstructor
public class TokenService {

    private final OAuth2Service oauth2Service;

    public OAuth2Token refreshToken(String refreshToken) {
        return oauth2Service.refreshToken(refreshToken);
    }
}
```

## Security Best Practices

1. **Token Storage**: Never store tokens in local storage. Use secure HTTP-only cookies when possible.

2. **Token Expiration**: Use short-lived access tokens (15-60 minutes) with refresh tokens.

3. **Scope Design**: Follow the principle of least privilege. Grant only necessary scopes.

4. **HTTPS Only**: Always use HTTPS in production to prevent token interception.

5. **Token Revocation**: Implement token revocation for logout and security incidents.

6. **Client Secrets**: Store client secrets securely using environment variables or secret management systems.

7. **Rate Limiting**: Combine OAuth2 with rate limiting to prevent token brute-force attacks.

## Performance Considerations

### Local Validation
- **Pros**: Fast, no network latency, no external dependency
- **Cons**: Requires token synchronization across gateway instances
- **Best for**: High-throughput APIs, internal services

### Remote Introspection
- **Pros**: Centralized token management, immediate revocation
- **Cons**: Network latency, external service dependency
- **Best for**: Multi-service architectures, public APIs

### Caching Strategy

Implement token caching for remote introspection:

```yaml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=10000,expireAfterWrite=5m
```

## Monitoring

Key metrics to monitor:

- Token validation success/failure rate
- Token expiration rate
- Scope validation failures
- Introspection endpoint latency (if using remote)
- Token generation rate

## Migration Guide

### From JWT Module

If migrating from JWT authentication:

1. Keep JWT module for backward compatibility
2. Add OAuth2 module with different paths
3. Gradually migrate clients to OAuth2
4. Update exclude patterns to avoid conflicts

```yaml
eraf:
  gateway:
    jwt:
      exclude-patterns:
        - /oauth2/**
    oauth2:
      exclude-patterns:
        - /jwt/**
```

## Dependencies

This module depends on:
- `eraf-gateway-common`: Common gateway components
- `eraf-core`: ERAF core utilities
- Spring Web: HTTP request handling
- Jackson: JSON processing

## License

Copyright (c) 2025 ERAF Framework

## Support

For issues and questions:
- GitHub Issues: https://github.com/eraf/eraf-api-gateway/issues
- Documentation: https://docs.eraf.io/gateway/oauth2
