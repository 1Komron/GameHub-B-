package com.gamehubbot.engine.checkers.move;

import com.gamehubbot.engine.checkers.cell.CellPosition;

import java.util.List;

public record PossibleMove(
        CellPosition from,
        CellPosition to,
        boolean isCapture,
        List<CellPosition> capturedPieces
) {}
