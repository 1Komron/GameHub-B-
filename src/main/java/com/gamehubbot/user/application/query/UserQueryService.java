package com.gamehubbot.user.application.query;

import com.gamehubbot.common.utils.PageUtil;
import com.gamehubbot.common.websocket.presence.PresenceRegistry;
import com.gamehubbot.match.dto.data.RecentOpponentsData;
import com.gamehubbot.user.domain.entity.User;
import com.gamehubbot.user.dto.response.RecentOpponentsResponse;
import com.gamehubbot.user.exceptions.UserNotFoundException;
import com.gamehubbot.user.infrastructure.port.UserMatchPort;
import com.gamehubbot.user.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserQueryService {
    private final UserRepository repository;
    private final PresenceRegistry presenceRegistry;
    private final UserMatchPort userMatchPort;

    public User loadUserByTelegramId(@NonNull Long telegramId) {
        return repository.findByTelegramId(telegramId)
                .orElseThrow(() -> new UserNotFoundException(telegramId));
    }

    public PageUtil.PageResponse<RecentOpponentsResponse> getRecentOpponents(Integer pageNum, Integer pageSize, Long userId) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        Page<RecentOpponentsData> page = userMatchPort.getRecentOpponents(pageable, userId);

        List<RecentOpponentsResponse> reposneList = page.getContent()
                .stream()
                .map(opponent -> {
                    boolean isOnline = presenceRegistry.isOnline(opponent.userId());
                    return new RecentOpponentsResponse(
                            opponent.userId(),
                            opponent.name(),
                            opponent.lastPlayedAt(),
                            isOnline
                    );
                }).toList();

        return PageUtil.pageInfo(page, reposneList);
    }
}
