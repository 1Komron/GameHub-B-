package com.gamehubbot.user.infrastructure.port;

import com.gamehubbot.match.dto.data.RecentOpponentsData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserMatchPort {
    Page<RecentOpponentsData> getRecentOpponents(Pageable pageable, Long userTelegramId);
}
