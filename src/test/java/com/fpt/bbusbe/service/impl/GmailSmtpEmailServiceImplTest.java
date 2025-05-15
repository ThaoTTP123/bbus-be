package com.fpt.bbusbe.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class GmailSmtpEmailServiceImplTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private GmailSmtpEmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendEmail_Success() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendEmail("test@example.com", "Test Subject", "<p>Test Content</p>");

        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendEmail_Failure() throws MessagingException {
        when(javaMailSender.createMimeMessage()).thenThrow(new MessagingException("Error"));

        assertThrows(RuntimeException.class, () ->
                emailService.sendEmail("test@example.com", "Test Subject", "<p>Test Content</p>"));
    }
}
