package com.servx.servx.controller;

import com.servx.servx.dto.ChatConversationDTO;
import com.servx.servx.dto.ChatMessageDTO;
import com.servx.servx.entity.User;
import com.servx.servx.exception.UserNotFoundException;
import com.servx.servx.repository.UserRepository;
import com.servx.servx.service.ChatService;
import com.servx.servx.util.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository; // Assuming you have this

    // Endpoint to get the list of conversations for the Inbox view
    @GetMapping
    public List<ChatConversationDTO> getConversations(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Fetching conversations for user: {}", userDetails.getUsername());
        User user = userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return chatService.getUserConversations(user.getId());
    }

    // Endpoint to get message history for a specific conversation/request
    @GetMapping("/{requestId}/messages")
    public Page<ChatMessageDTO> getMessages(
            @PathVariable Long requestId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 30, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Fetching messages for request {} for user {}", requestId, userDetails.getUsername());
        User user = userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return chatService.getMessagesForRequest(requestId, user.getId(), pageable);
    }

    // Endpoint to mark messages in a conversation as read
    @PatchMapping("/{requestId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markConversationAsRead(
            @PathVariable Long requestId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Marking messages as read for request {} for user {}", requestId, userDetails.getUsername());
        User user = userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        chatService.markMessagesAsRead(requestId, user.getId());
    }
}