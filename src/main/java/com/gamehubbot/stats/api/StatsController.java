package com.gamehubbot.stats.api;

import com.gamehubbot.stats.dto.StatsView;
import com.gamehubbot.stats.infrastructure.repository.UserGameStatsRepository;
import com.gamehubbot.user.domain.entity.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final UserGameStatsRepository statsRepository;

    public StatsController(UserGameStatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    @GetMapping("/me")
    public List<StatsView> myStats(@AuthenticationPrincipal UserPrincipal principal) {
        return statsRepository.findByUserId(principal.getUserTelegramId()).stream()
                .map(StatsView::from)
                .toList();
    }
}
