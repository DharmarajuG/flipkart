package shop.krishna.gateway.filter;

import io.jsonwebtoken.Claims;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import shop.krishna.common.security.JwtService;
import shop.krishna.gateway.config.GatewaySecurityProperties;

import java.util.List;

/**
 * Edge authentication. For non-public routes it requires a valid Bearer JWT,
 * then strips any client-supplied identity headers and injects trusted
 * {@code X-User-Id} / {@code X-User-Roles} headers for downstream services.
 *
 * <p>Downstream services trust these headers precisely because the gateway is the
 * only network path to them (enforced by security groups / NetworkPolicies).
 */
@Component
@EnableConfigurationProperties(GatewaySecurityProperties.class)
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtService jwtService;
    private final GatewaySecurityProperties securityProps;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(JwtService jwtService, GatewaySecurityProperties securityProps) {
        this.jwtService = jwtService;
        this.securityProps = securityProps;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing bearer token");
        }

        String token = authHeader.substring(7);
        if (!jwtService.isValid(token)) {
            return unauthorized(exchange, "Invalid or expired token");
        }

        Claims claims = jwtService.claims(token);
        Long userId = jwtService.userId(claims);
        List<String> roles = jwtService.roles(claims);

        // Overwrite (never trust) inbound identity headers.
        ServerHttpRequest mutated = request.mutate()
                .headers(h -> {
                    h.remove("X-User-Id");
                    h.remove("X-User-Roles");
                    h.remove("X-User-Name");
                })
                .header("X-User-Id", userId == null ? "" : String.valueOf(userId))
                .header("X-User-Roles", String.join(",", roles))
                .header("X-User-Name", claims.getSubject())
                .build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private boolean isPublic(String path) {
        return securityProps.getPublicPaths().stream().anyMatch(p -> pathMatcher.match(p, path));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String reason) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("X-Auth-Error", reason);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        // Run before routing but after Spring Cloud Gateway's built-in filters.
        return -1;
    }
}
