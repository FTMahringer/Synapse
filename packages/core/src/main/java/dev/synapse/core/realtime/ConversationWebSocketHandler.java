package dev.synapse.core.realtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.synapse.core.infrastructure.event.SynapseEvent;
import dev.synapse.core.infrastructure.event.SynapseEventType;
import dev.synapse.core.infrastructure.event.SynapseSpringEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for streaming conversation events to dashboard clients.
 * Broadcasts MESSAGE_SENT, MESSAGE_RECEIVED, CONVERSATION_STARTED, CONVERSATION_ENDED.
 */
@Component
public class ConversationWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ConversationWebSocketHandler.class);

    private static final Set<SynapseEventType> CONVERSATION_TYPES = Set.of(
        SynapseEventType.MESSAGE_SENT,
        SynapseEventType.MESSAGE_RECEIVED,
        SynapseEventType.CONVERSATION_STARTED,
        SynapseEventType.CONVERSATION_ENDED
    );

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final ObjectMapper objectMapper;

    public ConversationWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.debug("WebSocket client connected: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.debug("WebSocket client disconnected: {}", session.getId());
    }

    @Async
    @EventListener
    public void onSynapseEvent(SynapseSpringEvent springEvent) {
        SynapseEvent event = springEvent.getSynapseEvent();
        if (!CONVERSATION_TYPES.contains(event.type())) return;

        String json;
        try {
            json = objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            return;
        }

        TextMessage message = new TextMessage(json);
        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) {
                sessions.remove(session);
                continue;
            }
            try {
                session.sendMessage(message);
            } catch (IOException e) {
                sessions.remove(session);
                log.debug("WebSocket session removed after send failure: {}", session.getId());
            }
        }
    }

    public int activeConnections() {
        return sessions.size();
    }
}
