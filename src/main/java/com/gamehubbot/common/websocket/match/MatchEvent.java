package com.gamehubbot.common.websocket.match;


import tools.jackson.databind.JsonNode;

import java.util.UUID;

public record MatchEvent(String type, UUID matchId, Object state, Object winner) {
    public static MatchEvent matchUpdated(UUID matchId, Object state) {
        return new MatchEvent("MATCH_UPDATED", matchId, state, null);
    }

    public static MatchEvent matchFinished(UUID matchId, Object state, Object winner) {
        return new MatchEvent("MATCH_FINISHED", matchId, state, winner);
    }

    public static MatchEvent playerJoined(UUID matchId) {
        return new MatchEvent("PLAYER_JOINED", matchId, null, null);
    }

    public static MatchEvent playerLeft(UUID matchId, Object state) {
        return new MatchEvent("PLAYER_LEFT", matchId, state, null);
    }

    public static MatchEvent playerReady(UUID matchId, Object players) {
        return new MatchEvent("PLAYER_READY", matchId, players, null);
    }

    public static MatchEvent matchStarted(UUID id, JsonNode state) {
        return new MatchEvent("MATCH_STARTED", id, state, null);
    }

    public static MatchEvent matchCancelled(UUID matchId) {
        return new MatchEvent("MATCH_CANCELLED", matchId, null, null);
    }
}
