package com.gamehubbot.auth.application;

import com.gamehubbot.user.application.query.UserQueryService;
import com.gamehubbot.user.domain.entity.User;
import com.gamehubbot.user.domain.entity.UserPrincipal;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserQueryService userQueryService;

    @NonNull
    @Override
    public UserDetails loadUserByUsername(@NonNull String userTelegramId) throws UsernameNotFoundException {
        User user = getUser(Long.parseLong(userTelegramId));

        return new UserPrincipal(
                user.getTelegramId(),
                user.getUsername(),
                user.getFirstName()
        );

    }

    private User getUser(@org.jspecify.annotations.NonNull Long userTelegramId) {
        return userQueryService.loadUserByTelegramId(userTelegramId);
    }
}
