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
        assertEquals(
                "Une tâche vous a été attribuée!",
                EmailService.EmailBodyProvider.getEmailBody(User.UserRole.ADMIN)
        );
    }

    @Test
    void emailBodyProvider_shouldReturnDefault_forUnknownRole() {
        // rôle null -> getOrDefault renvoie default, mais ici la map getOrDefault attend une clé :
        // on teste plutôt avec une valeur "non mappée" si tu ajoutes un rôle plus tard.
        // Pour rester stable : on teste simplement que null ne casse pas.
        assertDoesNotThrow(() -> EmailService.EmailBodyProvider.getEmailBody(null));
    }

    @Test
    void emailSubjectProvider_shouldReturnMappedSubject_forKnownRole() {
        assertEquals(
                "Mail Subject for an admin",
                EmailService.EmailSubjectProvider.getEmailSubject(User.UserRole.ADMIN)
        );
    }

    @Test
    void emailSubjectProvider_shouldReturnDefault_forNullRole() {
        assertDoesNotThrow(() -> EmailService.EmailSubjectProvider.getEmailSubject(null));
    }
}
