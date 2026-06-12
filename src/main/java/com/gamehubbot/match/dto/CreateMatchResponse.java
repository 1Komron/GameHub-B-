package com.gamehubbot.match.dto;

import java.util.UUID;

public record CreateMatchResponse(UUID matchId, String joinCode) {
}
