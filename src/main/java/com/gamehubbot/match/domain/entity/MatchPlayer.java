package com.gamehubbot.match.domain.entity;

import com.gamehubbot.match.domain.enums.PlayerRole;
import com.gamehubbot.match.exceptions.PlayerNotCreatorException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Random;
import java.util.UUID;

@Entity
@Table(
        name = "match_players",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_match_players_match_user", columnNames = {"match_id", "user_id"}),
                @UniqueConstraint(name = "uk_match_players_match_seat", columnNames = {"match_id", "seat"})
        }
)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
@Setter
public class MatchPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JoinColumn(name = "match_id", nullable = false)
    private UUID matchId;

    @JoinColumn(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer seat;

    private Boolean isReady;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PlayerRole playerRole;

    public MatchPlayer(UUID matchId, Long userId, Integer seat, Boolean isReady, PlayerRole playerRole) {
        this.matchId = matchId;
        this.userId = userId;
        this.seat = seat;
        this.isReady = isReady;
        this.playerRole = playerRole;
    }

    public static MatchPlayer create(UUID matchId, Long userId, Integer seat, Boolean isReady, PlayerRole playerRole) {
        return new MatchPlayer(matchId, userId, seat, isReady, playerRole);
    }

    public void readyUp() {
        this.isReady = true;
    }

    public void ensureCreator() {
        if (playerRole != PlayerRole.CREATOR) {
            throw new PlayerNotCreatorException("Faqat yaratuvchi START qila oladi");
        }
    }

    public boolean isCreator() {
        return playerRole == PlayerRole.CREATOR;
    }
}
