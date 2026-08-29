package shop.krishna.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds the shared JWT settings. Every service that validates tokens (and the
 * user-service that issues them) reads the SAME secret and issuer, injected via
 * the config-server / environment so the HS256 signature is verifiable everywhere.
 */
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    /** Base64-encoded HMAC secret (>= 32 bytes decoded). Injected from Secrets Manager in prod. */
    private String secret;

    /** Access-token lifetime in milliseconds. Default 1 hour. */
    private long accessTokenExpirationMs = 3_600_000L;

    /** Refresh-token lifetime in milliseconds. Default 7 days. */
    private long refreshTokenExpirationMs = 604_800_000L;

    /** Token issuer claim. */
    private String issuer = "krishna.shop";

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public long getAccessTokenExpirationMs() { return accessTokenExpirationMs; }
    public void setAccessTokenExpirationMs(long v) { this.accessTokenExpirationMs = v; }

    public long getRefreshTokenExpirationMs() { return refreshTokenExpirationMs; }
    public void setRefreshTokenExpirationMs(long v) { this.refreshTokenExpirationMs = v; }

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
}
