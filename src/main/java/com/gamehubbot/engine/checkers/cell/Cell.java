package com.gamehubbot.engine.checkers.cell;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gamehubbot.engine.checkers.piece.Piece;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class Cell {

    private Piece piece;
    private boolean possibleMove;
    private int row;
    private int col;

    private CellColor color;

    @JsonCreator
    public Cell(
            @JsonProperty("piece") Piece piece,
            @JsonProperty("row") int row,
            @JsonProperty("col") int col,
            @JsonProperty("possibleMove") boolean possibleMove,
            @JsonProperty("color") CellColor color
    ) {
        this.piece = piece;
        this.possibleMove = possibleMove;
        this.row = row;
        this.col = col;
        this.color = color;
    }

    public Cell(CellColor color, int row, int col) {
        this.color = color;
        this.row = row;
        this.col = col;
    }

    public void move(Piece piece) {
        this.piece = piece;
    }

    public void clear() {
        this.piece = null;
    }
}
