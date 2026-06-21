package com.api.artezans.notifications.mail.dto;

import lombok.Builder;

@Builder
public record MailInfo(

        String name,

        String email
) {}
