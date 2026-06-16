package com.gamehubbot.engine;

import java.util.Arrays;
import java.util.List;

public record GameResult(
        boolean finished,
        Integer winnerSeat,
        List<Integer> winnerPosition,
        boolean draw
) {
    public static GameResult ongoing() {
        return new GameResult(false, null, null, false);
    }

    public static GameResult win(int winnerSeat, int[] winnerPosition) {
        List<Integer> list = Arrays.stream(winnerPosition)
                .boxed()
                .toList();
        return new GameResult(true, winnerSeat, list, false);
    }

    public static GameResult drawn() {
        return new GameResult(true, null, null, true);
    }

    public void ensureFinished() {
        if (finished) {
            throw new IllegalStateException("Match is already finished");
        }
    }
}
