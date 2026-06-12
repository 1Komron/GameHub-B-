package com.gamehubbot.auth.dto;

public record AuthResponse(String accessToken, UserView user) {
}
