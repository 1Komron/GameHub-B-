package com.gamehubbot.auth.dto;

import com.gamehubbot.user.domain.entity.User;

public record UserView(Long telegramId, String username, String firstName) {
    public static UserView from(User user) {
        return new UserView(user.getTelegramId(), user.getUsername(), user.getFirstName());
    }
}
