package com.api.artezans.notifications.sms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class SmsRequest {

    private String recipient;

    private String sender;

    private String content;
}
