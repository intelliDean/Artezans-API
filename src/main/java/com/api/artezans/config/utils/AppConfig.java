package com.api.artezans.config.utils;

import com.api.artezans.multimedia.CloudinaryMultimediaServiceImpl;
import com.api.artezans.multimedia.MultimediaService;
import com.api.artezans.notifications.mail.BrevoMailImpl;
import com.api.artezans.notifications.mail.MailService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.thymeleaf.context.Context;

import java.security.Key;

import static org.modelmapper.config.Configuration.AccessLevel.PRIVATE;
import static org.modelmapper.convention.MatchingStrategies.STANDARD;

@Configuration
public class AppConfig {
    @Value("${cloudinary.cloud.name}")
    private String cloudName;

    @Value("${cloudinary.api.secret}")
    private String apiSecret;

    @Value("${cloudinary.api.key}")
    private String cloudApiKey;

    @Value("${task.hub.secret.key}")
    private String SECRET;

    @Bean
    public ModelMapper mapper() {
        final ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(PRIVATE)
                .setSkipNullEnabled(true)
                .setMatchingStrategy(STANDARD);
        return mapper;
    }


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
    public MailService mailService() {
        return new BrevoMailImpl(getWebClientBuilder());
    }

    @Bean
    public MultimediaService multimediaService() {
        return new CloudinaryMultimediaServiceImpl(cloudinary());
    }

    @Bean
    public Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}