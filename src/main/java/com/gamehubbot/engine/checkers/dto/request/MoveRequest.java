package com.gamehubbot.engine.checkers.dto.request;

import com.gamehubbot.engine.checkers.cell.CellPosition;

public record MoveRequest(
        CellPosition from,
        CellPosition to
) {
}
