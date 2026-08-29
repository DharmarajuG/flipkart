package shop.krishna.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Trusts the identity headers injected by the API gateway ({@code X-User-Id},
 * {@code X-User-Roles}, {@code X-User-Name}) and populates the Spring Security
 * context. Downstream services never re-parse the JWT — the gateway already did.
 *
 * <p>This is safe because backend services are only reachable through the gateway
 * (enforced at the network layer). The principal is the numeric user id.
 */
public class GatewayHeaderAuthenticationFilter extends OncePerRequestFilter {

    public static final String H_USER_ID = "X-User-Id";
    public static final String H_USER_ROLES = "X-User-Roles";
    public static final String H_USER_NAME = "X-User-Name";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String userId = request.getHeader(H_USER_ID);
        if (userId != null && !userId.isBlank()
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String rolesHeader = request.getHeader(H_USER_ROLES);
            List<SimpleGrantedAuthority> authorities = (rolesHeader == null || rolesHeader.isBlank())
                    ? List.of()
                    : Arrays.stream(rolesHeader.split(","))
                        .map(String::trim)
                        .filter(r -> !r.isEmpty())
                        .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                        .map(SimpleGrantedAuthority::new)
                        .toList();

            var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
            authentication.setDetails(request.getHeader(H_USER_NAME));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response);
    }
}
