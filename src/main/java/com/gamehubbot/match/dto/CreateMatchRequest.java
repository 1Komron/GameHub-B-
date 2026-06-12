package com.gamehubbot.match.dto;

import com.gamehubbot.game.domain.enums.GameCode;
import jakarta.validation.constraints.NotNull;

public record CreateMatchRequest(@NotNull GameCode gameCode) {
}
