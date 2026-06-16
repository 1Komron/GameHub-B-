package com.gamehubbot.game.infrastructure;

import com.gamehubbot.game.domain.entity.Game;
import com.gamehubbot.game.domain.enums.GameCode;
import com.gamehubbot.game.infrastructure.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class GameSeeder implements CommandLineRunner {

    private final GameRepository gameRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seed(GameCode.TIC_TAC_TOE, "Tic Tac Toe", true);
        seed(GameCode.TIC_TAC_TOE_SHIFT, "Tic Tac Toe Shift", true);
        seed(GameCode.CHECKERS, "Checkers", false);
        seed(GameCode.BOWLING, "Bowling", false);
        seed(GameCode.CHESS, "Chess", false);
        seed(GameCode.CONNECT_FOUR, "Connect Four", false);
    }

    private void seed(GameCode code, String title, boolean enabled) {
        if (gameRepository.findByCode(code).isEmpty()) {
            gameRepository.save(new Game(code, title, enabled));
        }
    }
}
