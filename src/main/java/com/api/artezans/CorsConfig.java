package com.api.artezans;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NotNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedMethods("*")
                        .allowedOrigins("*");

//                registry.addMapping("/**")
//                        .allowedOriginPatterns("*", "http://localhost:3000", "http://localhost:8080")
//                        .allowedMethods("HEAD", "GET", "PUT", "POST", "DELETE", "PATCH", "OPTIONS")
//                        .allowedHeaders("Origin", "Content-Type", "Accept", "Authorization",
//                                "SECRET_KEY", "Access-Control-Allow-Credentials")
//                        .allowCredentials(true);
//                WebMvcConfigurer.super.addCorsMappings(registry);
            }
        };
    }
}