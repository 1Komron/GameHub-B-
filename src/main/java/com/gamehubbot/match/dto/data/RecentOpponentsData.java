package com.gamehubbot.match.dto.data;

import java.time.Instant;

public record RecentOpponentsData(
        Long userId,
        String name,
        Instant lastPlayedAt) {
}
