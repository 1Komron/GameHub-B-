package com.gamehubbot.game.domain.entity;

import com.gamehubbot.game.domain.enums.GameCode;
import com.gamehubbot.game.exceptions.GameNotEnabledException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "games")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private GameCode code;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Boolean enabled;

    public Game(GameCode code, String title, Boolean enabled) {
        this.code = code;
        this.title = title;
        this.enabled = enabled;
    }

    public void ensureEnabled() {
        if (!Boolean.TRUE.equals(enabled)) {
            throw new GameNotEnabledException("O'yin kativ emas. Code: " + code);
        }
    }

}
