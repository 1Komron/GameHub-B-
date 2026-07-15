package com.gamehubbot.match.application;

import com.gamehubbot.engine.GameResult;
import com.gamehubbot.game.domain.entity.Game;
import com.gamehubbot.game.domain.enums.GameCode;
import com.gamehubbot.game.exceptions.GameNotFoundException;
import com.gamehubbot.game.infrastructure.repository.GameRepository;
import com.gamehubbot.match.domain.entity.Match;
import com.gamehubbot.match.domain.entity.MatchPlayer;
import com.gamehubbot.match.domain.entity.MatchState;
import com.gamehubbot.match.dto.MatchView;
import com.gamehubbot.match.dto.PlayerView;
import com.gamehubbot.match.infrastructure.repository.MatchPlayerRepository;
import com.gamehubbot.match.infrastructure.repository.MatchRepository;
import com.gamehubbot.match.infrastructure.repository.MatchStateRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchSupplier {
    private final ObjectMapper objectMapper;
    private final MatchRepository matchRepository;
    private final MatchStateRepository stateRepository;
    private final MatchPlayerRepository playerRepository;
    private final GameRepository gameRepository;

    public MatchView toView(Match match) {
        List<PlayerView> players = playerRepository.findByMatchIdOrderBySeat(match.getId()).stream()
                .map(PlayerView::from)
                .toList();
        MatchState state = stateRepository.findById(match.getId())
                .orElseThrow(() -> new NoSuchElementException("Match state not found"));
        JsonNode jsonNode = readJsonNode(state.getStateJson());
        GameCode gameCode = gameRepository.findById(match.getGameId()).orElseThrow().getCode();

        return MatchView.toView(match, players, gameCode, jsonNode);
    }

    public Match loadMatch(UUID matchId) {
        return matchRepository.findById(matchId).orElseThrow(() -> new NoSuchElementException("Match not found"));
    }

    public Object readJson(String json) {
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception exception) {
            throw new IllegalStateException("Stored match state is invalid", exception);
        }
    }

    public JsonNode readJsonNode(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception exception) {
            throw new IllegalStateException("Stored match state is invalid", exception);
        }
    }

    public String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not serialize match data", exception);
        }
    }

    public String winner(GameResult result) {
        return result.winnerSeat() == null ? null : "PLAYER_" + result.winnerSeat();
    }

    public @NonNull Game loadByCode(GameCode gameCode) {
        return gameRepository.findByCode(gameCode)
                .orElseThrow(() -> new GameNotFoundException(gameCode));
    }

    public @NonNull MatchPlayer loadPlayer(UUID matchId, Long playerId) {
        return playerRepository.findByMatchIdAndUserId(matchId, playerId)
                .orElseThrow(() -> new IllegalStateException("User is not a player in this match"));
    }
}
