package com.gamehubbot.user.api;

import com.gamehubbot.common.ResponseMessage;
import com.gamehubbot.user.application.query.UserQueryService;
import com.gamehubbot.user.domain.entity.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserQueryService userQueryService;

    @GetMapping("/recent-opponents")
    public ResponseEntity<ResponseMessage> recentOpponents(@RequestParam(defaultValue = "1") Integer page,
                                                           @RequestParam(defaultValue = "10") Integer size,
                                                           @AuthenticationPrincipal UserPrincipal user) {
        var recentOpponents = userQueryService.getRecentOpponents(page, size, user.getUserTelegramId());
        return ResponseEntity.ok(ResponseMessage.success(null, recentOpponents));
    }
}
