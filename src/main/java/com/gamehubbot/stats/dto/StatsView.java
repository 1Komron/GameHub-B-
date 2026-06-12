package com.gamehubbot.stats.dto;

import com.gamehubbot.game.domain.enums.GameCode;
import com.gamehubbot.stats.domain.entity.UserGameStats;

public record StatsView(
        GameCode gameCode,
        int wins,
        int losses,
        int draws,
        int gamesPlayed
) {
    public static StatsView from(UserGameStats stats) {
        return new StatsView(
                null,
//                stats.getGame().getCode(),
                stats.getWins(),
                stats.getLosses(),
                stats.getDraws(),
                stats.getGamesPlayed()
        );
    }
}
