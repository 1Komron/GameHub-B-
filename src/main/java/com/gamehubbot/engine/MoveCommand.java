package com.gamehubbot.engine;

import tools.jackson.databind.JsonNode;

import java.util.UUID;

public record MoveCommand(
        UUID matchId,
        Long userId,
        int seat,
        JsonNode payload
) {
}
