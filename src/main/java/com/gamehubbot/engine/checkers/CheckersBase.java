package com.gamehubbot.engine.checkers;

import com.gamehubbot.game.domain.enums.GameCode;

public class CheckersBase {

    public GameCode gameCode() {
        return GameCode.CHECKERS;
    }

    public int maxPlayers() {
        return 2;
    }

    public CheckersState createInitialState() {
        return new CheckersState();
    }

}
