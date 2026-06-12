package com.gamehubbot.auth.dto;

import java.security.MessageDigest;

public final class MessageDigestSupport {
    private MessageDigestSupport() {
    }

    public static boolean constantTimeEquals(byte[] left, byte[] right) {
        return MessageDigest.isEqual(left, right);
    }
}
