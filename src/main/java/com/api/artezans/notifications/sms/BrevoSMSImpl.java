package com.api.artezans.notifications.sms;

import com.api.artezans.notifications.dto.SmsRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class BrevoSMSImpl implements SMSService {

    private final String apiKey;
    private final String appName;
    private final WebClient webClient;

    public BrevoSMSImpl(@Value("${sendinblue.mail.api_key}") String apiKey,
                        @Value("${sendinblue.sms.url}") String smsUrl,
                        @Value("${app.name}") String appName,
                        WebClient.Builder webClientBuilder) {
        this.apiKey = apiKey;
        this.appName = appName;
        this.webClient = webClientBuilder.clone()
                .baseUrl(smsUrl)
                .defaultHeader("api-key", apiKey)
                .defaultHeader("accept", "application/json")
                .defaultHeader("content-type", "application/json")
                .build();
    }

    @Override
    public String sendSms(SmsRequest request) {
        request.setSender(appName);

        return this.webClient.post()
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
