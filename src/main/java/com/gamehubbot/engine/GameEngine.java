package com.gamehubbot.engine;

import com.gamehubbot.game.domain.enums.GameCode;

public interface GameEngine {

    GameCode gameCode();

    int maxPlayers();

    Object createInitialState();

    Object applyMove(Object state, MoveCommand command);

    GameResult evaluate(Object state);
}
