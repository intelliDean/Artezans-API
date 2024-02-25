package com.api.artezans.notifications.sms;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsRequest {

    private String recipient;

    private String sender;

    private String content;
}
