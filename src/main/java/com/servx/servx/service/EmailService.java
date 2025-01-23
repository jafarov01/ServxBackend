package com.servx.servx.service;

import com.servx.servx.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service("emailService")
public class EmailService {
    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendVerificationEmail(User user, String token) {
        String verificationUrl = "http://localhost:8080/api/auth/verify-email?token=" + token;
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(user.getEmail());
        email.setSubject("Email Verification");
        email.setText("Please verify your email by clicking the link: " + verificationUrl);
        mailSender.send(email);
    }
}
