package com.example.project_management_tool.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${app.mail.enabled:false}")
    private boolean enabled;

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendTaskAssignedEmail(String toEmail, String projectName, String taskTitle) {
        if (!enabled) return;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[PMT] Tâche assignée");
        message.setText(
                "Bonjour,\n\n" +
                        "Une tâche vient de t’être assignée : " + taskTitle + "\n" +
                        "Projet : " + projectName + "\n\n" +
                        "— PMT"
        );

        mailSender.send(message);
    }
}
