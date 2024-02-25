package com.api.artezans.notifications.mail;


import com.api.artezans.notifications.mail.dto.EmailRequest;

public interface MailService {
   void sendMail(EmailRequest request);
}
