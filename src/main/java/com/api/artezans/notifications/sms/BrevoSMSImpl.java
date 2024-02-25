package com.api.artezans.notifications.sms;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrevoSMSImpl implements SMSService {
    private final WebClient.Builder webClient;

//    @Getter
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

    public String sendSmsWithOkhttp(SmsRequest smsRequest) throws IOException {
        smsRequest.setSender(appName);
        OkHttpClient client = new OkHttpClient();

        String content = new ObjectMapper().writeValueAsString(smsRequest);

        Request request = new Request.Builder()
                .url(smsUrl)
                .post(RequestBody.create(MediaType.parse("application/json"), content))
                .addHeader("accept", "application/json")
                .addHeader("content-type", "application/json")
                .addHeader("api-key", apiKey)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}


