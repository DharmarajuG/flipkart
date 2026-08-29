package shop.krishna.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/** Paths that bypass edge JWT validation (login, public catalog, actuator, docs). */
@ConfigurationProperties(prefix = "gateway.security")
public class GatewaySecurityProperties {
    private List<String> publicPaths = new ArrayList<>();

    public List<String> getPublicPaths() { return publicPaths; }
    public void setPublicPaths(List<String> publicPaths) { this.publicPaths = publicPaths; }
}
