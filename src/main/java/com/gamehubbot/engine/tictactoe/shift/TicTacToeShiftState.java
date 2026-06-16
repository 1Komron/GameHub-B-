package com.gamehubbot.engine.tictactoe.shift;

import com.gamehubbot.engine.GameResult;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;

@Getter
@Setter
@ToString
public class TicTacToeShiftState {
    private List<Move> board = new ArrayList<>(Collections.nCopies(9, null));
    private int currentSeat = 0;
    private Integer winnerSeat;
    private List<Integer> winnerPosition;
    private boolean draw;
    Integer expiringCell;
    Integer deletedCell;
    int totalMoves = 0;

    public void ensurePlayerTurn(int seat) {
        if (seat != currentSeat) {
            throw new IllegalArgumentException("It is not this player's turn");
        }
    }

    public void move(int seat, int position) {
        Move boardPosition = board.get(position);

        if (boardPosition != null) {
            throw new IllegalArgumentException("Cell is already occupied");
        }

        board.set(position, Move.move(seat, position, ++totalMoves));


    }

    public GameResult hasWinner(int[][] winLines) {
        for (int[] line : winLines) {
            Move m0 = board.get(line[0]);
            Move m1 = board.get(line[1]);
            Move m2 = board.get(line[2]);

            if (m0 != null && m1 != null && m2 != null
                    && m0.getSeat() == m1.getSeat()
                    && m1.getSeat() == m2.getSeat()) {
                GameResult result = GameResult.win(m0.getSeat(), line);

                announceWinner(result);

                return result;
            }
        }

        if (board.stream().allMatch(Objects::nonNull)) {
            announceDraw();
            return GameResult.drawn();
        }

        this.currentSeat = this.currentSeat == 0 ? 1 : 0;
        return GameResult.ongoing();
    }

    public void announceWinner(GameResult result) {
        this.deletedCell = null;
        this.expiringCell = null;
        this.winnerSeat = result.winnerSeat();
        this.winnerPosition = result.winnerPosition();
        draw = false;
    }

    public void announceDraw() {
        this.deletedCell = null;
        this.expiringCell = null;
        this.winnerPosition = null;
        this.winnerSeat = null;
        draw = true;
    }

    public void getExpiringMove() {
        if (totalMoves < 6) return;

        int nextSeat = currentSeat == 0 ? 1 : 0;

        board.stream()
                .filter(m -> m != null && m.getSeat() == nextSeat)
                .min(Comparator.comparingInt(Move::getMoveNumber))
                .ifPresent(m -> expiringCell = m.getCell());
    }

    public void deleteOldestMove() {
        if (totalMoves <= 6) return;

        board.stream()
                .filter(m -> m != null && m.getSeat() == currentSeat)
                .min(Comparator.comparingInt(Move::getMoveNumber))
                .ifPresent(m -> {
                    deletedCell = m.getCell();
                    board.set(deletedCell, null);
                });
    }


    public void changeSeat() {
        this.currentSeat = this.currentSeat == 0 ? 1 : 0;
    }
}
