package com.gamehubbot.match.infrastructure.repository;

import com.gamehubbot.match.domain.entity.MatchPlayer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchPlayerRepository extends JpaRepository<MatchPlayer, UUID> {
    List<MatchPlayer> findByMatchIdOrderBySeat(UUID matchId);

    Optional<MatchPlayer> findByMatchIdAndUserId(UUID matchId, Long userId);

    long countByMatchId(UUID matchId);
}
