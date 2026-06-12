package com.gamehubbot.common;

public record ResponseMessage(
        Boolean success,
        String msg,
        Object data
) {
    public static ResponseMessage success(String msg, Object data) {
        return new ResponseMessage(true, msg, data);
    }
}
