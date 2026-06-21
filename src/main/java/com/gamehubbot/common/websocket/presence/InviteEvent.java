package com.gamehubbot.common.websocket.presence;

public record InviteEvent(String type, Long fromUserId, String fromName, String matchId, String gameId) {
    public static InviteEvent of(Long fromUserId, String fromName, String matchId, String gameId) {
        return new InviteEvent("INVITE", fromUserId, fromName, matchId, gameId);
    }

    public static InviteEvent of(Long fromUserId, String fromName, String matchId) {
        return new InviteEvent("INVITE", fromUserId, fromName, matchId, null);
    }
}
