package com.gamehubbot.engine.checkers.move;


import com.gamehubbot.engine.checkers.dto.request.MoveRequest;
import com.gamehubbot.engine.tictactoe.MoveCommand;

public record MoveCheckers(
        MoveRequest moveRequest
) implements MoveCommand {
}
