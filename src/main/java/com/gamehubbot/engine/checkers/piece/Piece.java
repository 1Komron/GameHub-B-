package com.gamehubbot.engine.checkers.piece;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Piece {
    private final PieceColor color;
    private PieceType type;

    @JsonCreator
    public Piece(
            @JsonProperty("color") PieceColor color,
            @JsonProperty("type") PieceType type
    ) {
        this.color = color;
        this.type = type;
    }

    public Piece(PieceColor color) {
        this.color = color;
        this.type = PieceType.PAWN;
    }

    public Piece promoteToKing() {
        if (type == PieceType.KING) return this;
        this.type = PieceType.KING;
        return this;
    }

    public boolean isKing() {
        return type == PieceType.KING;
    }
}
