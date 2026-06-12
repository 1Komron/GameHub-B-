package com.gamehubbot.match.dto;

import tools.jackson.databind.JsonNode;
import com.gamehubbot.game.domain.enums.GameCode;
import com.gamehubbot.match.domain.enums.MatchStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MatchView(
        UUID matchId,
        GameCode gameCode,
        String joinCode,
        MatchStatus status,
        Instant startedAt,
        Instant finishedAt,
        List<PlayerView> players,
        JsonNode state
) {
}
