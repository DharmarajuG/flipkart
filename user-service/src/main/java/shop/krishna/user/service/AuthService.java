package shop.krishna.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.krishna.common.error.ConflictException;
import shop.krishna.common.error.UnauthorizedException;
import shop.krishna.common.security.JwtProperties;
import shop.krishna.common.security.JwtService;
import shop.krishna.user.domain.Role;
import shop.krishna.user.domain.User;
import shop.krishna.user.dto.AuthResponse;
import shop.krishna.user.dto.LoginRequest;
import shop.krishna.user.dto.RegisterRequest;
import shop.krishna.user.dto.UserResponse;
import shop.krishna.user.repository.UserRepository;

import java.util.List;

import io.jsonwebtoken.Claims;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email().toLowerCase())) {
            throw new ConflictException("Email already registered: " + req.email());
        }
        User user = User.builder()
                .email(req.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(req.password()))
                .fullName(req.fullName())
                .phone(req.phone())
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();
        user = userRepository.save(user);
        return issueTokens(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (!user.isEnabled() || !passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        return issueTokens(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(String refreshToken) {
        if (!jwtService.isValid(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        Claims claims = jwtService.claims(refreshToken);
        if (!"REFRESH".equals(claims.get("type"))) {
            throw new UnauthorizedException("Not a refresh token");
        }
        Long userId = jwtService.userId(claims);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User no longer exists"));
        return issueTokens(user);
    }

    private AuthResponse issueTokens(User user) {
        List<String> roles = List.of(user.getRole().name());
        String access = jwtService.generateAccessToken(user.getEmail(), user.getId(), roles);
        String refresh = jwtService.generateRefreshToken(user.getEmail(), user.getId());
        return new AuthResponse(access, refresh, "Bearer",
                jwtProperties.getAccessTokenExpirationMs(), UserResponse.from(user));
    }
}
