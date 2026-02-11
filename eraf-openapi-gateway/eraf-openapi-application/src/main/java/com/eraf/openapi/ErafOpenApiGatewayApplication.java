package com.eraf.openapi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * ERAF OpenAPI Gateway Application
 * 🌟 World's Best API Gateway - ERAF's Pride 🌟
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "com.eraf")
public class ErafOpenApiGatewayApplication {

    private final Environment env;

    public ErafOpenApiGatewayApplication(Environment env) {
        this.env = env;
    }

    public static void main(String[] args) {
        SpringApplication.run(ErafOpenApiGatewayApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() throws UnknownHostException {
        String protocol = env.getProperty("server.ssl.key-store") != null ? "https" : "http";
        String serverPort = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        String hostAddress = InetAddress.getLocalHost().getHostAddress();

        log.info("""

                ============================================================
                🌟 ERAF OpenAPI Gateway Started Successfully! 🌟
                ============================================================
                Application:    {}
                Profile(s):     {}
                Local:          {}://localhost:{}{}
                External:       {}://{}:{}{}
                Admin API:      {}://localhost:{}{}/admin
                Actuator:       {}://localhost:{}{}/actuator
                ============================================================
                """,
                env.getProperty("spring.application.name"),
                env.getActiveProfiles().length > 0 ? String.join(", ", env.getActiveProfiles()) : "default",
                protocol, serverPort, contextPath,
                protocol, hostAddress, serverPort, contextPath,
                protocol, serverPort, contextPath,
                protocol, serverPort, contextPath
        );
    }
}
