package com.gamehubbot.game.infrastructure.repository;

import com.gamehubbot.game.domain.entity.Game;
import com.gamehubbot.game.domain.enums.GameCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GameRepository extends JpaRepository<Game, UUID> {
    Optional<Game> findByCode(GameCode code);
}
