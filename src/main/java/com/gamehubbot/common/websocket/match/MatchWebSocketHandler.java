package com.gamehubbot.common.websocket.match;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class MatchWebSocketHandler extends TextWebSocketHandler {

    private final MatchWebSocketBroadcaster broadcaster;
    private final Map<String, UUID> sessionMatches = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        UUID matchId = extractMatchId(session.getUri());
        sessionMatches.put(session.getId(), matchId);
        broadcaster.register(matchId, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        UUID matchId = sessionMatches.remove(session.getId());
        if (matchId != null) {
            broadcaster.unregister(matchId, session);
        }
    }

    private UUID extractMatchId(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("WebSocket URI is missing");
        }
        String path = uri.getPath();
        String prefix = "/ws/matches/";
        if (!path.startsWith(prefix)) {
            throw new IllegalArgumentException("WebSocket path must be /ws/matches/{matchId}");
        }
        return UUID.fromString(path.substring(prefix.length()));
    }
}
