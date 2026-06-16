package com.gamehubbot.engine;

import tools.jackson.databind.JsonNode;

import java.util.UUID;

public record MoveCommand(
        UUID matchId,
        Long userId,
        int seat,
        JsonNode payload
) {
    public void ensureCellParam() {
        if (!payload.has("cell") || !payload.get("cell").canConvertToInt()) {
            throw new IllegalArgumentException("Move payload must contain integer field 'cell'");
        }
    }

    public int getCell() {
        int cell = payload.get("cell").asInt();

        if (cell < 0 || cell > 8) {
            throw new IllegalArgumentException("Cell must be between 0 and 8");
        }

        return cell;
    }
}
