package com.gamehubbot.match.application.management;

import com.gamehubbot.common.websocket.presence.InviteEvent;
import com.gamehubbot.common.websocket.presence.PresenceRegistry;
import com.gamehubbot.game.domain.enums.GameCode;
import com.gamehubbot.match.application.MatchJoinCodeGenerator;
import com.gamehubbot.match.application.MatchSupplier;
import com.gamehubbot.match.exceptions.MatchNotFoundException;
import com.gamehubbot.user.domain.entity.UserPrincipal;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.gamehubbot.engine.GameEngine;
import com.gamehubbot.engine.GameEngineRegistry;
import com.gamehubbot.engine.GameResult;
import com.gamehubbot.engine.tictactoe.MoveTicTacToe;
import com.gamehubbot.game.domain.entity.Game;
import com.gamehubbot.game.infrastructure.repository.GameRepository;
import com.gamehubbot.match.domain.entity.Match;
import com.gamehubbot.match.domain.entity.MatchPlayer;
import com.gamehubbot.match.domain.entity.MatchState;
import com.gamehubbot.match.domain.entity.Move;
import com.gamehubbot.match.domain.enums.MatchStatus;
import com.gamehubbot.match.dto.CreateMatchRequest;
import com.gamehubbot.match.dto.CreateMatchResponse;
import com.gamehubbot.match.dto.MatchView;
import com.gamehubbot.match.infrastructure.repository.MatchPlayerRepository;
import com.gamehubbot.match.infrastructure.repository.MatchRepository;
import com.gamehubbot.match.infrastructure.repository.MatchStateRepository;
import com.gamehubbot.match.infrastructure.repository.MoveRepository;
import com.gamehubbot.stats.application.StatsService;
import com.gamehubbot.common.websocket.match.MatchEvent;
import com.gamehubbot.common.websocket.match.MatchWebSocketBroadcaster;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.gamehubbot.match.domain.enums.PlayerRole.CREATOR;
import static com.gamehubbot.match.domain.enums.PlayerRole.PLAYER;
import static com.gamehubbot.match.exceptions.MatchNotFoundException.*;

@Service
@RequiredArgsConstructor
public class MatchManagement {
    private final GameRepository gameRepository;
    private final MatchRepository matchRepository;
    private final MatchPlayerRepository playerRepository;
    private final MatchStateRepository stateRepository;
    private final MoveRepository moveRepository;
    private final GameEngineRegistry engineRegistry;
    private final ObjectMapper objectMapper;
    private final MatchWebSocketBroadcaster broadcaster;
    private final StatsService statsService;
    private final MatchJoinCodeGenerator joinCodeGenerator;
    private final PresenceRegistry presenceRegistry;
    private final MatchSupplier supplier;

    private final Random random = new Random();

    @Transactional
    public CreateMatchResponse createMatch(CreateMatchRequest request, Long creatorId) {
        Game game = supplier.loadByCode(request.gameCode());

        game.ensureEnabled();

        GameEngine engine = engineRegistry.get(game.getCode());

        String joinCode = joinCodeGenerator.generateJoinCode();
        Match match = Match.create(game.getId(), joinCode);
        matchRepository.save(match);

        int seat = random.nextBoolean() ? 0 : 1;
        MatchPlayer player = MatchPlayer.create(match.getId(), creatorId, seat, true, CREATOR);
        playerRepository.save(player);

        MatchState matchState = MatchState.create(match, supplier.writeJson(engine.createInitialState()));
        stateRepository.save(matchState);

        return new CreateMatchResponse(match.getId(), joinCode);
    }

    @Transactional
    public MatchView joinMatch(UUID matchId, Long userTelegramId) {
        Match match = supplier.loadMatch(matchId);

        if (match.getStatus() != MatchStatus.WAITING) {
            return supplier.toView(match);
        }

        if (playerRepository.findByMatchIdAndUserId(matchId, userTelegramId).isPresent()) {
            return supplier.toView(match);
        }

        GameCode gameCode = gameRepository.findById(match.getGameId())
                .orElseThrow().getCode();

        GameEngine engine = engineRegistry.get(gameCode);

        List<MatchPlayer> existingPlayers = playerRepository.findByMatchIdOrderBySeat(matchId);
        if (existingPlayers.size() >= engine.maxPlayers()) {
            throw new IllegalStateException("Match is full");
        }

        int seat = nextSeat(existingPlayers, engine.maxPlayers());
        MatchPlayer newPlayer = MatchPlayer.create(matchId, userTelegramId, seat, false, PLAYER);
        playerRepository.save(newPlayer);

        MatchView view = supplier.toView(match);
        broadcaster.broadcast(matchId, MatchEvent.playerJoined(matchId));
        return view;
    }

    @Transactional
    public MatchView joinMatch(String joinCode, Long userTelegramId) {
        Match match = matchRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new MatchNotFoundException(NF_BY_CODE + joinCode));

        return joinMatch(match.getId(), userTelegramId);
    }


    @Transactional(readOnly = true)
    public MatchView getMatch(UUID matchId) {
        Match match = supplier.loadMatch(matchId);
        return supplier.toView(match);
    }

    @Transactional
    public MatchView makeMove(UUID matchId, String payloadJson, Long userTelegramId) {
        JsonNode payload = objectMapper.readTree(payloadJson);

        Match match = supplier.loadMatch(matchId);

        match.ensureActive();

        MatchPlayer player = supplier.loadPlayer(matchId, userTelegramId);

        MatchState matchState = stateRepository.findById(matchId)
                .orElseThrow(() -> new NoSuchElementException("Match state not found"));

        GameEngine engine = engineRegistry.get(gameRepository.findById(match.getGameId()).orElseThrow().getCode());

        Object currentState = supplier.readJson(matchState.getStateJson());

        Object nextState = engine.applyMove(currentState, new MoveTicTacToe(matchId, userTelegramId, player.getSeat(), payload));

        String nextStateJson = supplier.writeJson(nextState);

        matchState.setStateJson(nextStateJson);

        int moveNumber = Math.toIntExact(moveRepository.countByMatchId(matchId) + 1);
        moveRepository.save(new Move(match, player, moveNumber, supplier.writeJson(payload)));

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

    @Transactional
    public void readyMatch(UUID matchId, Long playerId) {
        Match match = supplier.loadMatch(matchId);

        match.ensureWaiting();

        MatchPlayer player = supplier.loadPlayer(matchId, playerId);

        player.readyUp();
        playerRepository.save(player);

        MatchView view = supplier.toView(match);
        broadcaster.broadcast(matchId, MatchEvent.playerReady(matchId, view.players()));
    }

    @Transactional
    public void leaveMatch(UUID matchId, Long playerId) {
        Match match = supplier.loadMatch(matchId);

        match.ensureWaiting();

        MatchPlayer player = supplier.loadPlayer(matchId, playerId);

        playerRepository.delete(player);

        if (player.isCreator()) {
            match.cancelMatch();
            matchRepository.save(match);
            broadcaster.broadcast(matchId, MatchEvent.matchCancelled(matchId));
            return;
        }

        MatchView view = supplier.toView(match);
        broadcaster.broadcast(matchId, MatchEvent.playerLeft(matchId, view.state()));
    }

    @Transactional
    public void startMatch(UUID id, Long userTelegramId) {
        Match match = supplier.loadMatch(id);

        if (match.getStatus() != MatchStatus.WAITING) {
            throw new IllegalStateException("Match is not in waiting state");
        }

        MatchPlayer player = supplier.loadPlayer(id, userTelegramId);

        player.ensureCreator();

        match.startMatch();

        matchRepository.save(match);

        MatchView view = supplier.toView(match);
        broadcaster.broadcast(id, MatchEvent.matchStarted(id, view.state()));
    }

    @Transactional
    public boolean inviteUser(UUID matchId, Long userId, UserPrincipal user) {
        Match match = supplier.loadMatch(matchId);

        match.ensureWaiting();

        InviteEvent event = InviteEvent.of(
                user.getUserTelegramId(),
                user.getFirstName(),
                matchId.toString(),
                gameRepository.findById(match.getGameId())
                        .orElseThrow()
                        .getCode()
                        .name());

        return presenceRegistry.sendToUser(userId, event);
    }

    private int nextSeat(List<MatchPlayer> players, int maxPlayers) {
        Set<Integer> occupied = new HashSet<>();
        for (MatchPlayer player : players) {
            occupied.add(player.getSeat());
        }
        for (int seat = 0; seat < maxPlayers; seat++) {
            if (!occupied.contains(seat)) {
                return seat;
            }
        }
        throw new IllegalStateException("Match is full");
    }
}
