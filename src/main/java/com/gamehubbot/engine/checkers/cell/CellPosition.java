package com.gamehubbot.engine.checkers.cell;

public record CellPosition(int row, int col) {
    public boolean isOnBoard() {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
}
