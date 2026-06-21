package com.api.artezans.config;

import com.api.artezans.config.security.AppUserDetailsService;
import com.api.artezans.config.security.JwtService;
import com.api.artezans.config.utils.APIError;
import com.api.artezans.config.utils.NoAuth;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

import static com.api.artezans.utils.ArtezanUtils.BEARER;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Component
@AllArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AppUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;




    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        AntPathMatcher matcher = new AntPathMatcher();
        return Arrays.stream(NoAuth.whiteList())
                .anyMatch(pattern -> matcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7); // 'Bearer ' length makes it 7

            if (!jwtService.validateToken(token)) {
                setErrorResponse(HttpStatus.UNAUTHORIZED, response,
                        new JwtException("Token is invalid or expired"));
                return;
            }

            String username = jwtService.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (userDetails.isEnabled()) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Authenticated user: {}", username);
                }
            }

            filterChain.doFilter(request, response);

        } catch (UsernameNotFoundException e) {
            log.error("User not found: {}", e.getMessage());
            setErrorResponse(HttpStatus.UNAUTHORIZED, response, e);
        } catch (JwtException e) {
            log.error("JWT error: {}", e.getMessage());
            setErrorResponse(HttpStatus.UNAUTHORIZED, response, e); // 401 not 400
        } catch (RuntimeException e) {
            log.error("Unexpected filter error: {}", e.getMessage());
            setErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, response, e);
        }
    }

    public void setErrorResponse(HttpStatus status,
                                 HttpServletResponse response,
                                 Throwable exception) {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        APIError apiError = new APIError(status, exception.getLocalizedMessage(), exception);
        try {
            response.getWriter().write(objectMapper.writeValueAsString(apiError));
        } catch (IOException e) {
            log.error("Failed to write error response: {}", e.getMessage());
        }
    }
}