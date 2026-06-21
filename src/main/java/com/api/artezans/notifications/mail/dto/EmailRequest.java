package com.api.artezans.notifications.mail.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class EmailRequest {

    private MailInfo sender;

    private List<MailInfo> to;

    private String subject;

    private String htmlContent;
}
