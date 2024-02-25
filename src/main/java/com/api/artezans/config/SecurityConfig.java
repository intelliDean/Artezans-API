package com.api.artezans.config;

import com.api.artezans.config.Oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.api.artezans.config.Oauth2.OAuth2AuthenticationFailureHandler;
import com.api.artezans.config.utils.NoAuth;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.http.HttpMethod;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class SecurityConfig implements WebMvcConfigurer {
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final UserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;


    //    private final Oauth2CustomUserService customOAuth2UserService;
//    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(authenticationEntryPoint))
                .authorizeHttpRequests().requestMatchers(
                        NoAuth.whiteList()).permitAll()
                .requestMatchers(NoAuth.swagger()).permitAll()
              .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("api/v1/user/deactivate").authenticated()
                .anyRequest().authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
//                .rememberMe()
//                .tokenRepository(new JdbcTokenRepositoryImpl())
//                .tokenValiditySeconds(604800)
                //    .and()
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
//                .oauth2Login(oauth2Login ->
//                        oauth2Login
//                                .authorizationEndpoint(authorizationEndpoint ->
//                                        authorizationEndpoint.baseUri("/auth/oauth2/authorize")
//                                                .authorizationRequestRepository(cookieAuthorizationRequestRepository())
//                                )
//                                .redirectionEndpoint(redirectionEndpoint ->
//                                        redirectionEndpoint.baseUri("/login/oauth2/code/*")
//                                )
//                                .userInfoEndpoint(userInfoEndpoint ->
//                                        userInfoEndpoint.userService(customOAuth2UserService)
//                                )
//                                .successHandler(oAuth2AuthenticationSuccessHandler)
//                                .failureHandler(oAuth2AuthenticationFailureHandler)
//                );
        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedOriginPatterns("*", "http://localhost:3000", "http://localhost:8080")
//                .allowedMethods("HEAD", "GET", "PUT", "POST", "DELETE", "PATCH", "OPTIONS")
//                .allowedHeaders("Origin", "Content-Type", "Accept", "Authorization",
//                        "SECRET_KEY", "Access-Control-Allow-Credentials")
//                .allowCredentials(true);
//        WebMvcConfigurer.super.addCorsMappings(registry);
//    }


    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
