package com.example.project_management_tool.service;

import com.example.project_management_tool.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @AfterEach
    void cleanup() {
        EmailService.mailSender = null;
    }

    @Test
    void testMailSender_shouldReturnFalse_whenNull() {
        EmailService.mailSender = null;
        assertFalse(EmailService.testMailSender());
    }

    @Test
    void testMailSender_shouldReturnTrue_whenNotNull() {
        EmailService.mailSender = mock(JavaMailSender.class);
        assertTrue(EmailService.testMailSender());
    }

    @Test
    void sendEmail_shouldCallMailSender_withExpectedMessage() {
        JavaMailSender sender = mock(JavaMailSender.class);
        EmailService.mailSender = sender;

        User user = new User("john", "john@example.com", "pwd", User.UserRole.ADMIN);

        EmailService.sendEmail(user);

        verify(sender).send(any(SimpleMailMessage.class));
    }

    @Test
    void emailBodyProvider_shouldReturnMappedBody_forKnownRole() {
        String body = EmailService.EmailBodyProvider.getEmailBody(User.UserRole.ADMIN);
        assertNotNull(body);
        assertFalse(body.isBlank());
    }

    @Test
    void emailBodyProvider_shouldReturnNonNull_forNullRole() {
        String body = EmailService.EmailBodyProvider.getEmailBody(null);
        assertNotNull(body);
    }

    @Test
    void emailSubjectProvider_shouldReturnMappedSubject_forKnownRole() {
        String subject = EmailService.EmailSubjectProvider.getEmailSubject(User.UserRole.ADMIN);
        assertNotNull(subject);
        assertFalse(subject.isBlank());
    }

    @Test
    void emailSubjectProvider_shouldReturnNonNull_forNullRole() {
        String subject = EmailService.EmailSubjectProvider.getEmailSubject(null);
        assertNotNull(subject);
    }
}
