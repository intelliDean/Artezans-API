package com.api.artezans.notifications.sms;

import com.api.artezans.notifications.dto.SmsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrevoSMSImpl implements SMSService {

    private final WebClient.Builder webClient;

    @Value("${sendinblue.mail.api_key}")
    private String apiKey;

    @Value("${sendinblue.sms.url}")
    private String smsUrl;

    @Value("${app.name}")
    private String appName;

    @Override
    public String sendSms(SmsRequest request) {
        request.setSender(appName);

        return webClient
                .baseUrl(smsUrl)
                .defaultHeader("api-key", apiKey)
                .defaultHeader("accept", "application/json")
                .defaultHeader("content-type", "application/json")
                .build()
                .post()
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
