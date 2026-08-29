package shop.krishna.order.client;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import shop.krishna.common.security.GatewayHeaderAuthenticationFilter;

/**
 * Propagates the gateway-injected identity headers on outgoing Feign calls so the
 * downstream service (cart-service) authenticates the same user.
 */
@Configuration
public class FeignAuthForwardingConfig {

    @Bean
    public RequestInterceptor authForwardingInterceptor() {
        return template -> {
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
                var req = attrs.getRequest();
                forward(template, req.getHeader(GatewayHeaderAuthenticationFilter.H_USER_ID),
                        GatewayHeaderAuthenticationFilter.H_USER_ID);
                forward(template, req.getHeader(GatewayHeaderAuthenticationFilter.H_USER_ROLES),
                        GatewayHeaderAuthenticationFilter.H_USER_ROLES);
                forward(template, req.getHeader(GatewayHeaderAuthenticationFilter.H_USER_NAME),
                        GatewayHeaderAuthenticationFilter.H_USER_NAME);
            }
        };
    }

    private void forward(feign.RequestTemplate template, String value, String header) {
        if (value != null && !value.isBlank()) {
            template.header(header, value);
        }
    }
}
