package com.servx.servx.controller;

import com.servx.servx.dto.ChatMessageDTO;
import com.servx.servx.entity.ChatMessage;
import com.servx.servx.entity.User;
import com.servx.servx.exception.UserNotFoundException;
import com.servx.servx.repository.UserRepository;
import com.servx.servx.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j // Lombok logger
public class ChatWsController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate; // For sending messages over WebSocket
    private final UserRepository userRepository;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDTO chatMessageDto, Principal principal) {
        log.info("Received message via WS: {}", chatMessageDto);
        try {
            ChatMessageDTO savedMessageDto = chatService.saveMessage(chatMessageDto, principal.getName());

            String recipientEmail = savedMessageDto.getRecipientEmail();
            if (recipientEmail == null) {
                log.error("Recipient email is null for message ID {}, cannot send to user queue.", savedMessageDto.getId());
                return;
            }

            messagingTemplate.convertAndSendToUser(
                    recipientEmail,
                    "/queue/messages",
                    savedMessageDto
            );
            log.info("Message ID {} sent to user {} queue", savedMessageDto.getId(), recipientEmail);

        } catch (Exception e) {
            log.error("Error handling WS message: {}", e.getMessage(), e);
        }
    }
}
