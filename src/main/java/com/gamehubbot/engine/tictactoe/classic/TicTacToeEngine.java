package com.gamehubbot.engine.tictactoe.classic;

import com.gamehubbot.engine.tictactoe.TicTacToeBase;
import com.gamehubbot.engine.tictactoe.shift.TicTacToeShiftState;
import tools.jackson.databind.ObjectMapper;
import com.gamehubbot.engine.GameEngine;
import com.gamehubbot.engine.GameResult;
import com.gamehubbot.engine.MoveCommand;
import com.gamehubbot.game.domain.enums.GameCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class TicTacToeEngine extends TicTacToeBase implements GameEngine {
    private final ObjectMapper objectMapper;

    @Override
    public GameCode gameCode() {
        return GameCode.TIC_TAC_TOE;
    }

    @Override
    public Object createInitialState() {
        return new TicTacToeShiftState();
    }

    @Override
    public Object applyMove(Object state, MoveCommand command) {
        TicTacToeState ticTacToeState = objectMapper.convertValue(state, TicTacToeState.class);
        GameResult result = evaluate(ticTacToeState);

        result.ensureFinished();

        if (command.seat() != ticTacToeState.getCurrentSeat()) {
            throw new IllegalArgumentException("It is not this player's turn");
        }
        if (!command.payload().has("cell") || !command.payload().get("cell").canConvertToInt()) {
            throw new IllegalArgumentException("Move payload must contain integer field 'cell'");
        }

        int cell = command.payload().get("cell").asInt();
        if (cell < 0 || cell > 8) {
            throw new IllegalArgumentException("Cell must be between 0 and 8");
        }

        List<String> board = ticTacToeState.getBoard();
        if (board.get(cell) != null) {
            throw new IllegalArgumentException("Cell is already occupied");
        }

        board.set(cell, command.seat() == 0 ? "X" : "O");
        GameResult afterMove = evaluate(ticTacToeState);
        if (afterMove.finished()) {
            ticTacToeState.setWinnerSeat(afterMove.winnerSeat());
            ticTacToeState.setDraw(afterMove.draw());
            ticTacToeState.setWinnerPosition(afterMove.winnerPosition());
        } else {
            ticTacToeState.setCurrentSeat(command.seat() == 0 ? 1 : 0);
        }
        return ticTacToeState;
    }

    @Override
    public GameResult evaluate(Object state) {
        TicTacToeState ticTacToeState = objectMapper.convertValue(state, TicTacToeState.class);
        List<String> board = ticTacToeState.getBoard();
        for (int[] line : getWinLines()) {
            String mark = board.get(line[0]);
            if (mark != null && mark.equals(board.get(line[1])) && mark.equals(board.get(line[2]))) {
                return GameResult.win("X".equals(mark) ? 0 : 1, line);
            }
        }
        if (board.stream().allMatch(Objects::nonNull)) {
            return GameResult.drawn();
        }
        return GameResult.ongoing();
    }
}
