package com.gamehubbot.match.infrastructure.repository;

import com.gamehubbot.match.domain.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID> {
    @Query("select count(m) > 0 from Match m where m.joinCode = :joinCode")
    boolean existsJoinCode(String joinCode);

    @Query("select m from Match m where m.joinCode = :joinCode")
    Optional<Match> findByJoinCode(String joinCode);
}
