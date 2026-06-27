package com.api.artezans.notifications.mail;

import com.api.artezans.exceptions.ArtezanException;
import com.api.artezans.notifications.dto.AppProperties;
import com.api.artezans.notifications.dto.BrevoMailProperties;
import com.api.artezans.notifications.dto.EmailRequest;
import com.api.artezans.notifications.dto.MailInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@Profile("default")
public class BrevoMailImpl implements MailService {

    private final BrevoMailProperties brevoProperties;
    private final AppProperties appProperties;
    private final WebClient webClient;

    public BrevoMailImpl(BrevoMailProperties brevoProperties,
                         AppProperties appProperties,
                         WebClient.Builder webClientBuilder) {
        this.brevoProperties = brevoProperties;
        this.appProperties = appProperties;
        this.webClient = webClientBuilder.clone()
                .baseUrl(brevoProperties.getUrl())
                .defaultHeader("api-key", brevoProperties.getApiKey())
                .defaultHeader("accept", "application/json")
                .defaultHeader("content-type", "application/json")
                .build();
    }

    @Override
    public void sendMail(EmailRequest emailRequest) {
        EmailRequest requestWithSender = buildRequestWithSender(emailRequest);

        this.webClient.post()
                .bodyValue(requestWithSender)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response ->
                        log.info("Email sent successfully to: {}", emailRequest.getTo()))
                .doOnError(error ->
                        log.error("Failed to send email to {}: {}",
                                emailRequest.getTo(), error.getMessage()))
                .onErrorResume(error -> {
                    throw new ArtezanException("Error sending email: " + error.getMessage());
                })
                .subscribe();
    }

    private EmailRequest buildRequestWithSender(EmailRequest emailRequest) {
        emailRequest.setSender(
                MailInfo.builder()
                        .name(appProperties.getName())
                        .email(appProperties.getEmail())
                        .build()
        );
        return emailRequest;
    }
}