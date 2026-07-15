package com.gamehubbot.engine.checkers.application;

import com.gamehubbot.common.websocket.match.MatchEvent;
import com.gamehubbot.common.websocket.match.MatchWebSocketBroadcaster;
import com.gamehubbot.engine.GameEngine;
import com.gamehubbot.engine.GameEngineRegistry;
import com.gamehubbot.engine.GameResult;
import com.gamehubbot.engine.checkers.CheckersEngine;
import com.gamehubbot.engine.checkers.CheckersState;
import com.gamehubbot.engine.checkers.cell.CellPosition;
import com.gamehubbot.engine.checkers.dto.request.MoveRequest;
import com.gamehubbot.engine.checkers.move.MoveCheckers;
import com.gamehubbot.engine.checkers.move.PossibleMove;
import com.gamehubbot.engine.tictactoe.MoveTicTacToe;
import com.gamehubbot.game.infrastructure.repository.GameRepository;
import com.gamehubbot.match.application.MatchSupplier;
import com.gamehubbot.match.domain.entity.Match;
import com.gamehubbot.match.domain.entity.MatchPlayer;
import com.gamehubbot.match.domain.entity.MatchState;
import com.gamehubbot.match.domain.entity.Move;
import com.gamehubbot.match.infrastructure.repository.MatchPlayerRepository;
import com.gamehubbot.match.infrastructure.repository.MatchRepository;
import com.gamehubbot.match.infrastructure.repository.MatchStateRepository;
import com.gamehubbot.match.infrastructure.repository.MoveRepository;
import com.gamehubbot.stats.application.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckersManagement {
    private final GameRepository gameRepository;
    private final MatchRepository matchRepository;
    private final MatchPlayerRepository playerRepository;
    private final MatchStateRepository stateRepository;
    private final GameEngineRegistry engineRegistry;
    private final MatchWebSocketBroadcaster broadcaster;
    private final StatsService statsService;
    private final MatchSupplier supplier;
    private final CheckersEngine engine;
    private final ObjectMapper objectMapper;

    @Transactional
    public Object move(UUID matchId, Long userTelegramId, MoveRequest moveRequest) {
        Match match = supplier.loadMatch(matchId);

        match.ensureActive();

        MatchState matchState = stateRepository.findById(matchId)
                .orElseThrow(() -> new NoSuchElementException("Match state not found"));

        GameEngine engine = engineRegistry.get(gameRepository.findById(match.getGameId()).orElseThrow().getCode());

        CheckersState state = objectMapper.readValue(matchState.getStateJson(), CheckersState.class);

        Object nextState = engine.applyMove(state, new MoveCheckers(moveRequest));

        String nextStateJson = supplier.writeJson(nextState);

        matchState.setStateJson(nextStateJson);

        stateRepository.save(matchState);

        GameResult result = engine.evaluate(nextState);

        JsonNode stateNode = supplier.readJsonNode(nextStateJson);
        if (result.finished()) {
            match.finishMatch();
            List<MatchPlayer> players = playerRepository.findByMatchIdOrderBySeat(matchId);
            statsService.recordFinishedMatch(match, players, result);
            broadcaster.broadcast(matchId, MatchEvent.matchFinished(matchId, stateNode, supplier.winner(result)));
        } else {
            broadcaster.broadcast(matchId, MatchEvent.matchUpdated(matchId, stateNode));
        }
        return supplier.toView(match);
    }

    public List<PossibleMove> getPossibleMoves(UUID matchId, Long userTelegramId, CellPosition cellPosition) {
        return engine.getPossibleMoves(matchId, userTelegramId, cellPosition);
    }

    private Match loadMatch(UUID matchId) {
        return matchRepository.findById(matchId).orElseThrow(() -> new NoSuchElementException("Match not found"));
    }
}
