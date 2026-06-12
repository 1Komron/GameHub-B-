package com.gamehubbot.match.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "match_states")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
@Setter
public class MatchState {

    @Id
    private UUID matchId;

    @OneToOne(optional = false)
    @MapsId
    @JoinColumn(name = "match_id")
    private Match match;


    @Column(nullable = false)
    private String stateJson;

    @Column(nullable = false)
    private Instant updatedAt;

    public MatchState(Match match, String stateJson) {
        this.match = match;
        this.stateJson = stateJson;
    }

    public static MatchState create(Match match, String stateJson) {
        return new MatchState(match, stateJson);
    }

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }

}
