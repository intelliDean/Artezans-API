package com.api.artezans.config.utils;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.thymeleaf.context.Context;

import javax.crypto.SecretKey;

@Configuration
public class AppConfig {
    @Value("${cloudinary.cloud.name}")
    private String cloudName;

    @Value("${cloudinary.api.secret}")
    private String apiSecret;

    @Value("${cloudinary.api.key}")
    private String cloudApiKey;

    @Value("${artezan.security.jwt.secret}")
    private String SECRET;

    // @Bean
    // public ModelMapper mapper() {
    // final ModelMapper mapper = new ModelMapper();
    // mapper.getConfiguration()
    // .setFieldMatchingEnabled(true)
    // .setFieldAccessLevel(PRIVATE)
    // .setSkipNullEnabled(true)
    // .setMatchingStrategy(STANDARD);
    // return mapper;
    // }

    @Bean
    public Context context() {
        return new Context();
    }

    @Bean
    public WebClient.Builder getWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(
                ObjectUtils.asMap(
                        "cloud_name", cloudName,
                        "api_key", cloudApiKey,
                        "api_secret", apiSecret));
    }

    @Bean
    public com.fasterxml.jackson.databind.ObjectMapper objectMapper() {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.findAndRegisterModules();
        return mapper;
    }

    @Bean
    public SecretKey secretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}