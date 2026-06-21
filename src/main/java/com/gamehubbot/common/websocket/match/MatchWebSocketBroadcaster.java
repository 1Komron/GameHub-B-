package com.gamehubbot.common.websocket.match;

import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MatchWebSocketBroadcaster {

    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<UUID, Set<WebSocketSession>> sessionsByMatch = new ConcurrentHashMap<>();

    public MatchWebSocketBroadcaster(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void register(UUID matchId, WebSocketSession session) {
        sessionsByMatch.computeIfAbsent(matchId, ignored -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void unregister(UUID matchId, WebSocketSession session) {
        Set<WebSocketSession> sessions = sessionsByMatch.get(matchId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                sessionsByMatch.remove(matchId);
            }
        }
    }

    public void broadcast(UUID matchId, MatchEvent event) {
        Set<WebSocketSession> sessions = sessionsByMatch.get(matchId);
        System.out.println("[WS] Broadcasting " + event.type() + " to matchId=" + matchId + ", sessions=" + (sessions == null ? 0 : sessions.size()));
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        try {
            TextMessage message = new TextMessage(objectMapper.writeValueAsString(event));
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(message);
                    } catch (Exception ignored) {
                        sessions.remove(session);
                    }
                }
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Could not broadcast match event", exception);
        }
    }
}
