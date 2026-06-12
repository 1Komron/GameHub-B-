package com.gamehubbot.match.exceptions;

import com.gamehubbot.common.exceptions.NotFoundException;

public class MatchNotFoundException extends NotFoundException {
    public static final String NF_BY_CODE = "Mach topilmadi. CODE: ";
    public MatchNotFoundException(String message) {
        super(message);
    }
}
