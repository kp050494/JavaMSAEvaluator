package com.assessment.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

/**
 * Observes STOMP lifecycle events for the submission progress topic. Messages
 * themselves are published via {@link com.assessment.service.WebSocketService}
 * using the SimpMessagingTemplate; this handler only logs connectivity.
 */
@Component
public class SubmissionProgressHandler {

    private static final Logger log = LoggerFactory.getLogger(SubmissionProgressHandler.class);

    @EventListener
    public void onConnect(SessionConnectedEvent event) {
        log.debug("WebSocket client connected: {}", event.getMessage().getHeaders().get("simpSessionId"));
    }

    @EventListener
    public void onSubscribe(SessionSubscribeEvent event) {
        Object destination = event.getMessage().getHeaders().get("simpDestination");
        log.debug("WebSocket subscription to {}", destination);
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        log.debug("WebSocket client disconnected: {}", event.getSessionId());
    }
}
