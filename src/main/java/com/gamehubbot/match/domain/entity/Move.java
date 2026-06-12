package com.gamehubbot.match.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "moves",
        uniqueConstraints = @UniqueConstraint(name = "uk_moves_match_move_number", columnNames = {"match_id", "move_number"})
)
public class Move {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private MatchPlayer player;

    @Column(nullable = false)
    private Integer moveNumber;

    @Column(nullable = false)
    private String payload;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Move() {
    }

    public Move(Match match, MatchPlayer player, Integer moveNumber, String payload) {
        this.match = match;
        this.player = player;
        this.moveNumber = moveNumber;
        this.payload = payload;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public Match getMatch() {
        return match;
    }

    public MatchPlayer getPlayer() {
        return player;
    }

    public Integer getMoveNumber() {
        return moveNumber;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
