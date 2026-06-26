package com.api.artezans.notifications.sms;

import com.api.artezans.notifications.dto.SmsRequest;

public interface SMSService {

    String sendSms(SmsRequest request);

}

