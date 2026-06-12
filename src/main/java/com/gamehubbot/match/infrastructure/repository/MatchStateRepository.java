package com.gamehubbot.match.infrastructure.repository;

import com.gamehubbot.match.domain.entity.MatchState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MatchStateRepository extends JpaRepository<MatchState, UUID> {
}
