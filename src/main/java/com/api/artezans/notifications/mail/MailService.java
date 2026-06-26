package com.api.artezans.notifications.mail;


import com.api.artezans.notifications.dto.EmailRequest;

public interface MailService {
   void sendMail(EmailRequest request);
}
