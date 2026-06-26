package com.api.artezans.notifications.dto;

import lombok.Builder;

@Builder
public record MailInfo(

        String name,

        String email
) {}
