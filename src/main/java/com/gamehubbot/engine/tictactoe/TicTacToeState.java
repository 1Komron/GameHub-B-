package com.gamehubbot.engine.tictactoe;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class TicTacToeState {

    private List<String> board = new ArrayList<>(Collections.nCopies(9, null));
    private int currentSeat = 0;
    private Integer winnerSeat;
    private List<Integer> winnerPosition;
    private boolean draw;
}
