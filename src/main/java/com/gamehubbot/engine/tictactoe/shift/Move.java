package com.gamehubbot.engine.tictactoe.shift;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Move {

    private final int seat;
    private final int cell;
    private final int moveNumber;

    @JsonCreator
    private Move(
            @JsonProperty("seat") int seat,
            @JsonProperty("cell") int cell,
            @JsonProperty("moveNumber") int moveNumber
    ) {
        this.seat = seat;
        this.cell = cell;
        this.moveNumber = moveNumber;
    }

    public static Move move(int seat, int cell, int moveNumber) {
        return new Move(seat, cell, moveNumber);
    }
    
}
