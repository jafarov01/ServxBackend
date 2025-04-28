package com.servx.servx.service;

import com.servx.servx.dto.SupportRequestDTO;
import com.servx.servx.entity.User;
import com.servx.servx.exception.UserNotFoundException;
import com.servx.servx.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportService {

    private final JavaMailSender javaMailSender;
    private final UserRepository userRepository;

    private final String supportRecipientEmail = "makhlugjafarov@gmail.com";

    public void sendSupportRequest(UserDetails userDetails, SupportRequestDTO request) {
        String userEmail = userDetails.getUsername();
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> {
                    log.error("Support request failed: User not found with email {}", userEmail);
                    return new UserNotFoundException("User sending support request not found.");
                });

        String subject = String.format("Servx Support Request - User %d (%s)",
                user.getId(),
                userEmail);

        String body = buildEmailBody(user, request.getMessage());

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(supportRecipientEmail);
            mailMessage.setSubject(subject);
            mailMessage.setText(body);
            mailMessage.setReplyTo(userEmail);

            log.info("Attempting to send support email to {} from {}", supportRecipientEmail, userEmail);
            javaMailSender.send(mailMessage);
            log.info("Support email sent successfully for user ID: {}", user.getId());

        } catch (MailException e) {
            log.error("Failed to send support email for user ID {}: {}", user.getId(), e.getMessage(), e);
        }
    }

    private String buildEmailBody(User user, String userMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append("Support Request from Servx User:\n\n");
        sb.append("----------------------------------------\n");
        sb.append("User Details:\n");
        sb.append("  ID: ").append(user.getId()).append("\n");
        sb.append("  Name: ").append(user.getFirstName()).append(" ").append(user.getLastName()).append("\n");
        sb.append("  Email: ").append(user.getEmail()).append("\n");
        sb.append("  Phone: ").append(user.getPhoneNumber()).append("\n");
        sb.append("  Role: ").append(user.getRole() != null ? user.getRole().name() : "N/A").append("\n");
        sb.append("----------------------------------------\n\n");
        sb.append("User Message:\n");
        sb.append(userMessage).append("\n\n");
        sb.append("----------------------------------------\n");
        sb.append("Timestamp: ").append(Instant.now()).append("\n");
        return sb.toString();
    }
}
