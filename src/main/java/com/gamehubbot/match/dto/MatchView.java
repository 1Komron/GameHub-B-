package com.gamehubbot.match.dto;

import com.gamehubbot.match.domain.entity.Match;
import com.gamehubbot.match.domain.entity.MatchState;
import tools.jackson.databind.JsonNode;
import com.gamehubbot.game.domain.enums.GameCode;
import com.gamehubbot.match.domain.enums.MatchStatus;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
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
    public static MatchView toView(Match match, List<PlayerView> players, GameCode gameCode, JsonNode state) {
        return new MatchView(
                match.getId(),
                gameCode,
                match.getJoinCode(),
                match.getStatus(),
                match.getStartedAt(),
                match.getFinishedAt(),
                players,
                state
        );
    }
}
