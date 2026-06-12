package com.gamehubbot.match.domain.entity;

import com.gamehubbot.match.domain.enums.MatchStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "matches")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
@Setter
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JoinColumn(name = "game_id", nullable = false)
    private UUID gameId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status;

    @Column(unique = true, length = 7)
    private String joinCode;

    private Instant startedAt;

    private Instant finishedAt;

    public Match(UUID gameId, MatchStatus status, String joinCode) {
        this.gameId = gameId;
        this.status = status;
        this.joinCode = joinCode;
    }

    public static Match create(UUID gameId, String joinCode) {
        return new Match(gameId, MatchStatus.WAITING, joinCode);
    }

    public void ensureActive() {
        if (this.status != MatchStatus.ACTIVE) {
            throw new IllegalStateException("Match is not active");
        }
    }

    public void ensureWaiting() {
        if (this.status != MatchStatus.WAITING) {
            throw new IllegalStateException("Match is not waiting");
        }
    }

    public void startMatch() {
        this.status = MatchStatus.ACTIVE;
        this.startedAt = Instant.now();
    }

    public void finishMatch() {
        this.status = MatchStatus.FINISHED;
        this.finishedAt = Instant.now();
        this.joinCode = null;
    }

    public void cancelMatch() {
        this.status = MatchStatus.CANCELLED;
        this.joinCode = null;
    }
}
