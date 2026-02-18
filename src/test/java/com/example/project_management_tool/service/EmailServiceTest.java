package com.example.project_management_tool.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Test
    void sendTaskAssignedEmail_shouldDoNothing_whenDisabled() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailService emailService = new EmailService(mailSender);

        // enabled reste false par défaut -> aucun envoi
        emailService.sendTaskAssignedEmail("client@example.com", "PMT - Projet Démo", "Tache X");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendTaskAssignedEmail_shouldSendMail_whenEnabled() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailService emailService = new EmailService(mailSender);

        // Force enabled=true (sans démarrer Spring)
        ReflectionTestUtils.setField(emailService, "enabled", true);

        emailService.sendTaskAssignedEmail("client@example.com", "PMT - Projet Démo", "Tache X");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertNotNull(sent);

        assertArrayEquals(new String[]{"client@example.com"}, sent.getTo());
        assertEquals("[PMT] Tâche assignée", sent.getSubject());

        String text = sent.getText();
        assertNotNull(text);
        assertTrue(text.contains("Tache X"));
        assertTrue(text.contains("PMT - Projet Démo"));
    }
}
