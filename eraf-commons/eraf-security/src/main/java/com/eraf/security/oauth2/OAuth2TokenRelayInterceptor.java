package com.eraf.security.oauth2;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * OAuth2 Token Relay 인터셉터
 *
 * RestTemplate/RestClient 호출 시 현재 사용자의 OAuth2 토큰을 자동 전달합니다.
 *
 * 사용 예시:
 * <pre>
 * RestTemplate restTemplate = new RestTemplate();
 * restTemplate.getInterceptors().add(new OAuth2TokenRelayInterceptor(userInfoService));
 * </pre>
 */
public class OAuth2TokenRelayInterceptor implements ClientHttpRequestInterceptor {

    private final OAuth2UserInfoService userInfoService;

    public OAuth2TokenRelayInterceptor(OAuth2UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                         ClientHttpRequestExecution execution) throws IOException {
        String accessToken = userInfoService.getCurrentAccessToken();
        if (accessToken != null) {
            request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        }
        return execution.execute(request, body);
    }
}
