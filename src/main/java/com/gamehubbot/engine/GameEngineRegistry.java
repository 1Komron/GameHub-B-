package com.gamehubbot.engine;

import com.gamehubbot.game.domain.enums.GameCode;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class GameEngineRegistry {

    private final Map<GameCode, GameEngine> engines = new EnumMap<>(GameCode.class);

    public GameEngineRegistry(List<GameEngine> engineList) {
        for (GameEngine engine : engineList) {
            engines.put(engine.gameCode(), engine);
        }
    }

    public GameEngine get(GameCode gameCode) {
        GameEngine engine = engines.get(gameCode);
        if (engine == null) {
            throw new IllegalArgumentException("Game engine is not implemented: " + gameCode);
        }
        return engine;
    }
}
