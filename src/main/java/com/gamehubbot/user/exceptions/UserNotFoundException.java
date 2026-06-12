package com.gamehubbot.user.exceptions;

import com.gamehubbot.common.exceptions.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    private static final String MESSAGE_TEMPLATE = "User topilmadi. ID %d";

    public UserNotFoundException(Long userTelegramId) {
        super(MESSAGE_TEMPLATE.formatted(userTelegramId));
    }
}
