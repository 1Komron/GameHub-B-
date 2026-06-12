package com.gamehubbot.user.application.query;

import com.gamehubbot.user.domain.entity.User;
import com.gamehubbot.user.exceptions.UserNotFoundException;
import com.gamehubbot.user.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserQueryService {
    private final UserRepository repository;

    public User loadUserByTelegramId(@NonNull Long telegramId) {
        return repository.findByTelegramId(telegramId)
                .orElseThrow(() -> new UserNotFoundException(telegramId));
    }
}
