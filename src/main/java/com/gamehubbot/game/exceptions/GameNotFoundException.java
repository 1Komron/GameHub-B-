package com.gamehubbot.game.exceptions;

import com.gamehubbot.common.exceptions.NotFoundException;
import com.gamehubbot.game.domain.enums.GameCode;

public class GameNotFoundException extends NotFoundException {
    private static final String MSG = "O'yin topilmadi. CODE: ";

    public GameNotFoundException(GameCode code) {
        super(MSG + code);
    }
}
