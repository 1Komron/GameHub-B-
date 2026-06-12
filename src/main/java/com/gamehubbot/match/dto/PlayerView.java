package com.gamehubbot.match.dto;

import com.gamehubbot.match.domain.entity.MatchPlayer;

import java.util.UUID;

public record PlayerView(
        UUID playerId,
        Long userId,
        Integer seat,
        Boolean isReady
) {
    public static PlayerView from(MatchPlayer player) {
        return new PlayerView(
                player.getId(),
                player.getUserId(),
                player.getSeat(),
                player.getIsReady()
        );
    }
}
