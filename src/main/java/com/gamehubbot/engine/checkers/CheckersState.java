package com.gamehubbot.engine.checkers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gamehubbot.engine.checkers.cell.Cell;
import com.gamehubbot.engine.checkers.cell.CellColor;
import com.gamehubbot.engine.checkers.cell.CellPosition;
import com.gamehubbot.engine.checkers.piece.Piece;
import com.gamehubbot.engine.checkers.piece.PieceColor;
import lombok.Getter;

@Getter
public class CheckersState {
    private final Cell[][] cells;
    private PieceColor turn;

    public CheckersState() {
        this(createBoard(), PieceColor.WHITE);
    }

    @JsonCreator
    public CheckersState(
            @JsonProperty("cells") Cell[][] cells,
            @JsonProperty("turn") PieceColor turn
    ) {
        this.cells = cells;
        this.turn = turn;
    }

    private static Cell[][] createBoard() {
        Cell[][] board = new Cell[8][8];

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                CellColor color = (row + col) % 2 == 0 ? CellColor.WHITE : CellColor.BLACK;
                Cell cell = new Cell(color, row, col);

                if (color == CellColor.BLACK) {
                    if (row < 3) {
                        cell.move(new Piece(PieceColor.BLACK));
                    } else if (row > 4) {
                        cell.move(new Piece(PieceColor.WHITE));
                    }
                }

                board[row][col] = cell;
            }
        }

        return board;
    }

    public boolean checkTurn(int seat) {
        return (turn == PieceColor.WHITE && seat == 0) ||
                (turn == PieceColor.BLACK && seat == 1);
    }

    public PieceColor colorForPlayer(int seat) {
        return seat == 0 ? PieceColor.WHITE : PieceColor.BLACK;
    }

    public void switchTurn() {
        this.turn = this.turn == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;
    }


    public Cell getCell(CellPosition pos) {
        return cells[pos.row()][pos.col()];
    }

    public int seatForColor(PieceColor turn) {
        return turn == PieceColor.WHITE ? 0 : 1;
    }
}
