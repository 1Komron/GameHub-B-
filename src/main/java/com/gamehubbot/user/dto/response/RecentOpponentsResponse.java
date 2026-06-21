package com.gamehubbot.user.dto.response;

import java.time.Instant;

public record RecentOpponentsResponse(
        Long userId,
        String name,
        Instant lastPlayedAt,
        Boolean isOnline
) {
}
