package shop.krishna.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Framework-agnostic JWT issuing/parsing helper shared by all services.
 * user-service uses {@link #generateAccessToken}; every other service (and the
 * gateway) uses {@link #parse} / {@link #isValid} to authenticate requests.
 */
public class JwtService {

    private final SecretKey key;
    private final JwtProperties props;

    public JwtService(JwtProperties props) {
        this.props = props;
        // Secret is expected to be a strong random string (>= 32 chars). HS256.
        this.key = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String subject, Long userId, List<String> roles) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(subject)
                .issuer(props.getIssuer())
                .claim("uid", userId)
                .claim("roles", roles)
                .claim("type", "ACCESS")
                .issuedAt(new Date(now))
                .expiration(new Date(now + props.getAccessTokenExpirationMs()))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(String subject, Long userId) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(subject)
                .issuer(props.getIssuer())
                .claim("uid", userId)
                .claim("type", "REFRESH")
                .issuedAt(new Date(now))
                .expiration(new Date(now + props.getRefreshTokenExpirationMs()))
                .signWith(key)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(props.getIssuer())
                .build()
                .parseSignedClaims(token);
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims claims(String token) {
        return parse(token).getPayload();
    }

    @SuppressWarnings("unchecked")
    public List<String> roles(Claims claims) {
        Object roles = claims.get("roles");
        if (roles instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of();
    }

    public Long userId(Claims claims) {
        Object uid = claims.get("uid");
        return uid == null ? null : Long.valueOf(uid.toString());
    }

    public Map<String, Object> claimMap(String token) {
        return parse(token).getPayload();
    }
}
