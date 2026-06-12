package com.gamehubbot.auth.api;

import com.gamehubbot.auth.dto.AuthRequest;
import com.gamehubbot.auth.dto.AuthResponse;
import com.gamehubbot.auth.application.TelegramAuthService;
import com.gamehubbot.common.ResponseMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final TelegramAuthService telegramAuthService;

    public AuthController(TelegramAuthService telegramAuthService) {
        this.telegramAuthService = telegramAuthService;
    }

    @PostMapping("/telegram")
    public ResponseEntity<ResponseMessage> telegram(@RequestBody AuthRequest request) {
        var data = telegramAuthService.authenticate(request.initData());
        return ResponseEntity.ok(ResponseMessage.success("Muvaffaqiyatli", data));
    }
}
