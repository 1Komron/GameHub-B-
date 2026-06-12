package com.gamehubbot.stats.infrastructure.repository;

import com.gamehubbot.stats.domain.entity.UserGameStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserGameStatsRepository extends JpaRepository<UserGameStats, UUID> {
    Optional<UserGameStats> findByUserIdAndGameId(Long userId, UUID gameId);

    java.util.List<UserGameStats> findByUserId(Long userId);
}
