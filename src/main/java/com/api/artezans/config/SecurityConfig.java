package com.api.artezans.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.api.artezans.config.Oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.api.artezans.config.Oauth2.OAuth2AuthenticationFailureHandler;
import com.api.artezans.config.Oauth2.OAuth2AuthenticationSuccessHandler;
import com.api.artezans.config.Oauth2.userDetail.Oauth2CustomUserService;
import com.api.artezans.config.utils.ArtezanAccessDeniedHandler;
import com.api.artezans.config.utils.NoAuth;

import lombok.AllArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class SecurityConfig {
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final UserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;
    private final ArtezanAccessDeniedHandler accessDeniedHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    private final Oauth2CustomUserService oauth2CustomUserService;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        httpRequest -> httpRequest.requestMatchers(NoAuth.whiteList()).permitAll()
                                .requestMatchers(NoAuth.swagger()).permitAll()
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers("/api/v1/user/deactivate").authenticated()
                                .anyRequest().authenticated()) //anything path aside from the above, make sure it is authenticated
                .sessionManagement(sessionMgt -> sessionMgt.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2Login(oauth2 -> oauth2.authorizationEndpoint(authorization -> authorization.authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository))
                        .userInfoEndpoint(userInfo -> userInfo.userService(oauth2CustomUserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .build();
    }

    /**
     * Provides a {@link DaoAuthenticationProvider} as an explicit bean.
     * Spring Security's built-in implementation already handles UserDetailsService
     * lookup + password matching — no need for a custom reimplementation.
     * Declaring it here keeps the AuthenticationManager wiring clean and
     * eliminates the "UserDetailsService beans will not be used" warning.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        // return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        return new Argon2PasswordEncoder(
                16,
                32,
                1,
                65536,
                4);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
        return configuration.getAuthenticationManager();
    }
}
