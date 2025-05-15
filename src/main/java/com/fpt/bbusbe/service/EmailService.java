package com.fpt.bbusbe.service;

import org.springframework.scheduling.annotation.Async;

public interface EmailService {

    @Async
    void sendEmail(String to, String subject, String content);
}
