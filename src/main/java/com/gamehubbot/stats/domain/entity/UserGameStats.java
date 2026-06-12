package com.gamehubbot.stats.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(
        name = "user_game_stats",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_game_stats_user_game", columnNames = {"user_id", "game_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserGameStats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JoinColumn(name = "user_id", nullable = false)
    private Long userId;

    @JoinColumn(name = "game_id", nullable = false)
    private UUID gameId;

    @Column(nullable = false)
    private int wins;

    @Column(nullable = false)
    private int losses;

    @Column(nullable = false)
    private int draws;

    @Column(nullable = false)
    private int gamesPlayed;

    public UserGameStats(Long userId, UUID gameId) {
        this.userId = userId;
        this.gameId = gameId;
    }

    public void recordWin() {
        wins++;
        gamesPlayed++;
    }

    public void recordLoss() {
        losses++;
        gamesPlayed++;
    }

    public void recordDraw() {
        draws++;
        gamesPlayed++;
    }

}
