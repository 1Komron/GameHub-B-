package com.gamehubbot.common.websocket;

import com.gamehubbot.common.websocket.match.MatchWebSocketHandler;
import com.gamehubbot.common.websocket.presence.JwtHandshakeInterceptor;
import com.gamehubbot.common.websocket.presence.PresenceWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final MatchWebSocketHandler matchWebSocketHandler;
    private final PresenceWebSocketHandler presenceWebSocketHandler;
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(matchWebSocketHandler, "/ws/matches/{matchId}")
                .setAllowedOriginPatterns("*");

        registry.addHandler(presenceWebSocketHandler, "/ws/presence")
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOriginPatterns("*");
    }
}
