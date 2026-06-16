package com.gamehubbot.engine.tictactoe.shift;

import com.gamehubbot.engine.GameEngine;
import com.gamehubbot.engine.GameResult;
import com.gamehubbot.engine.MoveCommand;
import com.gamehubbot.engine.tictactoe.TicTacToeBase;
import com.gamehubbot.engine.tictactoe.classic.TicTacToeState;
import com.gamehubbot.game.domain.enums.GameCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class TicTacToeShiftEngine extends TicTacToeBase implements GameEngine {
    private final ObjectMapper objectMapper;

    @Override
    public GameCode gameCode() {
        return GameCode.TIC_TAC_TOE_SHIFT;
    }

    @Override
    public Object createInitialState() {
        return new TicTacToeState();
    }

    @Override
    public Object applyMove(Object state, MoveCommand command) {
        TicTacToeShiftState shiftState = objectMapper.convertValue(state, TicTacToeShiftState.class);
        GameResult result = evaluate(shiftState);

        result.ensureFinished();

        int seat = command.seat();
        shiftState.ensurePlayerTurn(seat);

        command.ensureCellParam();

        int cell = command.getCell();

        shiftState.move(seat, cell);

        shiftState.getExpiringMove();

        shiftState.deleteOldestMove();

        evaluate(shiftState);

        shiftState.hasWinner(getWinLines());

        return shiftState;
    }

    @Override
    public GameResult evaluate(Object state) {
        TicTacToeShiftState shiftState = objectMapper.convertValue(state, TicTacToeShiftState.class);

        return shiftState.hasWinner(getWinLines());
    }


}
