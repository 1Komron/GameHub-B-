package com.gamehubbot.match.infrastructure.adapter;

import com.gamehubbot.match.dto.data.RecentOpponentsData;
import com.gamehubbot.match.infrastructure.repository.MatchRepository;
import com.gamehubbot.user.infrastructure.port.UserMatchPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchAdapter implements UserMatchPort {
    private final MatchRepository repository;

    @Override
    public Page<RecentOpponentsData> getRecentOpponents(Pageable pageable, Long userId) {
        return repository.getFinishedMatchesHistory(pageable, userId);
    }
}
