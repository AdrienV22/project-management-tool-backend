package com.example.project_management_tool.service;

import com.example.project_management_tool.entity.User;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;


@Service
public class EmailService {

    public static JavaMailSender mailSender;

    public static boolean testMailSender() {
        if (mailSender != null) {
            System.out.println("MailSender bean is loaded successfully!");
            return TRUE;

        }
        System.out.println("MailSender bean is NOT loaded!");
        return FALSE;
    }


    public static void sendEmail(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject(EmailSubjectProvider.getEmailSubject(user.getUserRole()));
        message.setText(EmailBodyProvider.getEmailBody(user.getUserRole()));
        mailSender.send(message);
    }

    public static class EmailBodyProvider {

        private static final Map<User.UserRole, String> EMAIL_BODY_MAP = new HashMap<>();

        static {
            EMAIL_BODY_MAP.put(User.UserRole.ADMIN, "Mail Body for an admin");
            EMAIL_BODY_MAP.put(User.UserRole.MEMBRE, "Mail Body for a member");
            EMAIL_BODY_MAP.put(User.UserRole.OBSERVATEUR, "Mail Body for an outsider");
        }

        public static String getEmailBody(User.UserRole role) {
            return EMAIL_BODY_MAP.getOrDefault(role, "Default email body");
        }
    }

    public static class EmailSubjectProvider {

        private static final Map<User.UserRole, String> EMAIL_SUBJECT_MAP = new HashMap<>();

        static {
            EMAIL_SUBJECT_MAP.put(User.UserRole.ADMIN, "Mail Subject for an admin");
            EMAIL_SUBJECT_MAP.put(User.UserRole.MEMBRE, "Mail Subject for a member");
            EMAIL_SUBJECT_MAP.put(User.UserRole.OBSERVATEUR, "Mail Subject for an outsider");
        }

        public static String getEmailSubject(User.UserRole role) {
            return EMAIL_SUBJECT_MAP.getOrDefault(role, "Default email body");
        }
    }
}