package com.servx.servx.util;

import com.servx.servx.entity.User;
import com.servx.servx.enums.Role;
import com.servx.servx.exception.AuthenticationException;
import com.servx.servx.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

// AuthChannelInterceptor.java

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            log.info(">>> Intercepting STOMP CONNECT frame. SessionId: {}", accessor.getSessionId());
            Map<String, List<String>> nativeHeaders = accessor.toNativeHeaderMap();
            if (nativeHeaders != null && !nativeHeaders.isEmpty()) {
                log.info(">>> Received Native Headers in STOMP frame: {}", nativeHeaders);
            } else {
                log.warn(">>> No native headers found in STOMP frame!");
            }

            try {

                handleConnectFrame(accessor);
                log.info("<<< STOMP CONNECT Authentication SUCCESSFUL for SessionId: {}", accessor.getSessionId());
                return message;
            } catch (AuthenticationException e) {
                log.error("<<< STOMP CONNECT Authentication FAILED for SessionId: {}: {}", accessor.getSessionId(), e.getMessage());

                return null;
            }
        }
        return message;
    }

    private void handleConnectFrame(StompHeaderAccessor accessor) throws AuthenticationException {
        log.debug("handleConnectFrame: Attempting to get 'authorization' native header...");
        List<String> authHeaders = accessor.getNativeHeader("authorization");
        String token = extractBearerToken(authHeaders);

        if (token == null) {
            log.warn("Native header 'authorization' not found, trying 'Authorization'...");
            authHeaders = accessor.getNativeHeader("Authorization");
            token = extractBearerToken(authHeaders);

            if (token == null) {
                log.error("No Bearer token found in CONNECT frame under 'authorization' or 'Authorization'. Headers were: {}", accessor.toNativeHeaderMap());
                throw new AuthenticationCredentialsNotFoundException("Missing authorization header");
            }
        }

        if (!jwtUtils.validateToken(token)) {
            log.warn("Invalid JWT token found in STOMP CONNECT header.");
            throw new BadCredentialsException("Invalid token");
        }
        authenticateUser(token, accessor);
    }

    private String extractBearerToken(List<String> authHeaders) {
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String header = authHeaders.get(0);
            if (header.trim().startsWith("Bearer ")) {
                return header.trim().substring(7);
            }
        }
        return null;
    }

    private void authenticateUser(String token, StompHeaderAccessor accessor) {
        String email = jwtUtils.getEmailFromToken(token);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        if (!user.isVerified()) {
            throw new DisabledException("User not verified");
        }
        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        accessor.setUser(authentication);
        log.info("Authenticated STOMP user: {}", email);
    }
}