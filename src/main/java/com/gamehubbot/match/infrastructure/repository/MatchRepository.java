package com.gamehubbot.match.infrastructure.repository;

import com.gamehubbot.match.domain.entity.Match;
import com.gamehubbot.match.dto.data.RecentOpponentsData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID> {
    @Query("select count(m) > 0 from Match m where m.joinCode = :joinCode")
    boolean existsJoinCode(String joinCode);

    @Query("select m from Match m where m.joinCode = :joinCode")
    Optional<Match> findByJoinCode(String joinCode);

    @Query("""
            select new com.gamehubbot.match.dto.data.RecentOpponentsData(
                    opponent.telegramId,
                    opponent.firstName,
                    max(m.startedAt)
                )
            from Match m
            join MatchPlayer myPlayer on myPlayer.matchId = m.id and myPlayer.userId = :userId
            join MatchPlayer oppPlayer on oppPlayer.matchId = m.id and oppPlayer.userId <> :userId
            join User opponent on opponent.telegramId = oppPlayer.userId
            where m.status in ('FINISHED', 'CANCELLED')
            group by opponent.telegramId, opponent.firstName
            order by max(m.startedAt) desc
            """)
    Page<RecentOpponentsData> getFinishedMatchesHistory(Pageable pageable, Long userId);
}
