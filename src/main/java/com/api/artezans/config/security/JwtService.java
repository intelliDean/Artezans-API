package com.api.artezans.config.security;

import com.api.artezans.tokens.service.interfaces.TaskHubTokenService;
import com.api.artezans.users.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final TaskHubTokenService taskHubTokenService;
    private final Key key;

    @Value("${access_expiration}")
    private Long accessExpiration;
    @Value("${refresh_expiration}")
    private Long refreshExpiration;


    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isValid(String token) {
        try {
            final Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            final Date expiration = claims.getExpiration();
            return expiration != null &&
                    expiration.after(Date.from(Instant.now()));
        } catch (JwtException e) {
            return false;
        }
    }

    private Boolean isRevoked(String token) {
        return taskHubTokenService.isTokenValid(token);
    }

    public Boolean validateToken(String token) {
        return isValid(token) && isRevoked(token);
    }

    public Tokens generateToken(Authentication authentication) {
        SecuredUser securedUser = (SecuredUser) authentication.getPrincipal();
        String username = securedUser.getUsername();
        Map<String, Object> claims = getClaims(authentication);

        String accessToken = accessToken(claims, username);
        String refreshToken = refreshToken(username);

        return new Tokens(accessToken, refreshToken);
    }

    private String accessToken(Map<String, Object> claims, String userName) {
        return createToken(claims, userName, accessExpiration);
    }

    public String accessToken(User user) {
        return createToken(getClaims(user), user.getEmailAddress(), accessExpiration);
    }

    private String refreshToken(String userName) {
        return createToken(new HashMap<>(), userName, refreshExpiration);
    }


    private String createToken(Map<String, Object> claims, String userName, Long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userName)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(Duration.ofHours(expiration))))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Map<String, Object> getClaims(Authentication authentication) {
        Map<String, Object> claims = new HashMap<>();
        authentication.getAuthorities().forEach(authority -> claims.put("claim", authority));
        return claims;
    }

    public Map<String, Object> getClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .forEach(authority -> claims.put("claim", authority));
        return claims;
    }

    public record Tokens(String accessToken, String refreshToken) {}
}