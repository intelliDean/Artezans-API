package com.api.artezans.config.security;

import com.api.artezans.tokens.service.interfaces.ArtezanTokenService;
import com.api.artezans.users.models.User;
import com.api.artezans.users.models.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final ArtezanTokenService artezanTokenService;
    private final SecretKey secretKey;

    @Value("${access_expiration}")
    private Long accessExpiration;

    @Value("${refresh_expiration}")
    private Long refreshExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration() != null
                    && claims.getExpiration().after(Date.from(Instant.now()));
        } catch (JwtException e) {
            return false;
        }
    }

    private boolean isRevoked(String token) {
        return !artezanTokenService.isTokenValid(token);
    }

    public boolean validateToken(String token) {
        return isValid(token) && !isRevoked(token);
    }

    public Tokens generateToken(Authentication authentication) {
        SecuredUser securedUser = (SecuredUser) authentication.getPrincipal();

        assert securedUser != null;
        String username = securedUser.getUsername();
        Map<String, Object> claims = getClaims(securedUser.getUser());

        return new Tokens(
                accessToken(claims, username),
                refreshToken(username)
        );
    }

    public String accessToken(User user) {
        return createToken(getClaims(user), user.getEmailAddress(), accessExpiration);
    }

    private String accessToken(Map<String, Object> claims, String userName) {
        return createToken(claims, userName, accessExpiration);
    }

    private String refreshToken(String userName) {
        return createToken(new HashMap<>(), userName, refreshExpiration);
    }

    private String createToken(Map<String, Object> claims, String userName, Long expiration) {
        return Jwts.builder()
                .claims(claims)
                .subject(userName)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(Duration.ofHours(expiration))))
                .signWith(secretKey)
                .compact();
    }

    private Map<String, Object> getClaims(User user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::name)
                .toList();
        return Map.of("roles", roles);
    }

    public Map<String, Object> getClaims(Authentication authentication) {
        Map<String, Object> claims = new HashMap<>();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        claims.put("roles", roles);
        return claims;
    }

    public record Tokens(String accessToken, String refreshToken) {}
}