package com.api.artezans.notifications.mail;

import com.api.artezans.notifications.mail.dto.EmailRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("default")
public class TestMailImpl implements MailService {

    @Override
    public void sendMail(EmailRequest request) {
        log.info("Mail sent successfully to: {}", request.getTo().get(0).getEmail());
    }
}
