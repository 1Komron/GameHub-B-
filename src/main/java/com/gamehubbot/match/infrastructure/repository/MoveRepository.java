package com.gamehubbot.match.infrastructure.repository;

import com.gamehubbot.match.domain.entity.Move;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MoveRepository extends JpaRepository<Move, UUID> {
    long countByMatchId(UUID matchId);
}
