package com.gamehubbot.stats.application;

import com.gamehubbot.engine.GameResult;
import com.gamehubbot.game.domain.entity.Game;
import com.gamehubbot.game.infrastructure.repository.GameRepository;
import com.gamehubbot.match.domain.entity.Match;
import com.gamehubbot.match.domain.entity.MatchPlayer;
import com.gamehubbot.stats.domain.entity.UserGameStats;
import com.gamehubbot.stats.infrastructure.repository.UserGameStatsRepository;
import com.gamehubbot.user.domain.entity.User;
import com.gamehubbot.user.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final UserGameStatsRepository statsRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    public void recordFinishedMatch(Match match, List<MatchPlayer> players, GameResult result) {
        Game game = gameRepository.findById(match.getGameId())
                .orElseThrow(() -> new NoSuchElementException("Game not found"));
        for (MatchPlayer player : players) {
            User user = userRepository.findById(player.getUserId())
                    .orElseThrow(() -> new NoSuchElementException("User not found"));
            UserGameStats stats = statsRepository.findByUserIdAndGameId(player.getUserId(), match.getGameId())
                    .orElseGet(() -> new UserGameStats(user.getTelegramId(), game.getId()));
            if (result.draw()) {
                stats.recordDraw();
            } else if (player.getSeat().equals(result.winnerSeat())) {
                stats.recordWin();
            } else {
                stats.recordLoss();
            }
            statsRepository.save(stats);
        }
    }
}
