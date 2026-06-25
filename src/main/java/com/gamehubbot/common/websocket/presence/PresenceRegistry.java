package com.gamehubbot.common.websocket.presence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class PresenceRegistry {

    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<Long, WebSocketSession> sessionsByUser = new ConcurrentHashMap<>();

    public void connect(Long userId, WebSocketSession session) {
        WebSocketSession old = sessionsByUser.put(userId, session);
        if (old != null && old.isOpen() && old != session) {
            try {
                old.close(CloseStatus.NORMAL.withReason("New session opened"));
            } catch (Exception ignored) {
            }
        }
    }

    public void disconnect(Long userId, WebSocketSession session) {
        sessionsByUser.computeIfPresent(userId, (id, existing) ->
                existing == session ? null : existing
        );
    }

    public boolean isOnline(Long userId) {
        WebSocketSession session = sessionsByUser.get(userId);
        return session != null && session.isOpen();
    }

    public boolean sendToUser(Long userId, Object event) {
        WebSocketSession session = sessionsByUser.get(userId);
        if (session == null || !session.isOpen()) {
            return false;
        }
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(event)));
            return true;
        } catch (Exception exception) {
            sessionsByUser.remove(userId);
            return false;
        }
    }
}