package com.api.artezans.notifications.mail.dto;

import lombok.*;

import java.util.List;
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    private MailInfo sender;

    private List<MailInfo> to;

    private String subject;

    private String htmlContent;
}
