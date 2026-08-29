package shop.krishna.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import shop.krishna.common.security.JwtProperties;

/**
 * Single edge entry point for krishna.shop. Routes to backend services via Eureka
 * ({@code lb://}), enforces JWT at the edge, applies Redis-backed rate limiting,
 * and propagates identity headers downstream.
 */
@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
