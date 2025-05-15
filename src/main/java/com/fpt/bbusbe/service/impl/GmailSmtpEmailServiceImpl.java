package com.fpt.bbusbe.service.impl;

import com.fpt.bbusbe.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GmailSmtpEmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Override
    @Async
    public void sendEmail(String to, String subject, String contentHtml) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(contentHtml, true);
            helper.setFrom("bbusvn@gmail.com"); // đổi thành gmail thật của bạn

            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email OTP: " + e.getMessage(), e);
        }
    }
}
