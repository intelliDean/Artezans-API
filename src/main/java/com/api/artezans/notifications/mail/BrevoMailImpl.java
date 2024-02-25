package com.api.artezans.notifications.mail;


import com.api.artezans.notifications.mail.dto.EmailRequest;
import com.api.artezans.notifications.mail.dto.MailInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
@Slf4j
@Service
@Profile("!default")
@RequiredArgsConstructor
public class BrevoMailImpl implements MailService {
    private final WebClient.Builder webClient;

    @Value("${sendinblue.mail.api_key}")
    private String apiKey;

    @Value("${sendinblue.mail.url}")
    private String mailUrl;

    @Value("${app.name}")
    private String appName;

    @Value("${app.email}")
    private String appEmail;


    @Override
    @Async
    public void sendMail(EmailRequest emailRequest) {
        emailRequest.setSender(new MailInfo(appName, appEmail));
        webClient
                .baseUrl(mailUrl)
                .defaultHeader("api-key", apiKey)
                .build()
                .post()
                .bodyValue(emailRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
