package com.assessment.service;

import com.assessment.dto.ProgressMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Publishes submission progress to STOMP subscribers at
 * {@code /topic/submission/{sessionId}}.
 */
@Service
public class WebSocketService {

    public static final String TOPIC_PREFIX = "/topic/submission/";

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void send(String sessionId, ProgressMessage message) {
        messagingTemplate.convertAndSend(TOPIC_PREFIX + sessionId, message);
    }
}
