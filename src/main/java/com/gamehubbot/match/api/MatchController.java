package com.gamehubbot.match.api;

import com.gamehubbot.common.ResponseMessage;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.gamehubbot.match.application.management.MatchManagement;
import com.gamehubbot.match.dto.CreateMatchRequest;
import com.gamehubbot.user.domain.entity.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {
    private final MatchManagement matchManagement;

    @PostMapping
    public ResponseEntity<ResponseMessage> createMatch(@Valid @RequestBody CreateMatchRequest request,
                                                       @AuthenticationPrincipal UserPrincipal user) {
        var data = matchManagement.createMatch(request, user.getUserTelegramId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseMessage.success("Muvaffaqiyatli yaratildi", data));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<ResponseMessage> joinMatch(@PathVariable UUID id,
                                                     @AuthenticationPrincipal UserPrincipal user) {
        var data = matchManagement.joinMatch(id, user.getUserTelegramId());
        return ResponseEntity.ok(ResponseMessage.success("Muvaffaqiyatli qo'shildi", data));
    }

    @PostMapping("/join/{code}")
    public ResponseEntity<ResponseMessage> joinMatchByCode(@PathVariable String code,
                                                           @AuthenticationPrincipal UserPrincipal user) {
        var data = matchManagement.joinMatch(code, user.getUserTelegramId());
        return ResponseEntity.ok(ResponseMessage.success("Muvaffaqiyatli qo'shildi", data));
    }

    @PostMapping("/{id}/ready")
    public ResponseEntity<ResponseMessage> readyMatch(@PathVariable UUID id,
                                                      @AuthenticationPrincipal UserPrincipal user) {
        matchManagement.readyMatch(id, user.getUserTelegramId());
        return ResponseEntity.ok(ResponseMessage.success("Muvaffaqiyatli tayyorlandi", null));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ResponseMessage> startMatch(@PathVariable UUID id,
                                                      @AuthenticationPrincipal UserPrincipal user) {
        matchManagement.startMatch(id, user.getUserTelegramId());
        return ResponseEntity.ok(ResponseMessage.success("Muvaffaqiyatli boshlandi", null));
    }

    @PostMapping("/{id}/moves")
    public ResponseEntity<ResponseMessage> makeMove(@PathVariable UUID id,
                                                    @RequestBody String payload,
                                                    @AuthenticationPrincipal UserPrincipal user) {
        var data = matchManagement.makeMove(id, payload, user.getUserTelegramId());
        return ResponseEntity.ok(ResponseMessage.success("Muvaffaqiyatli", data));
    }

    @DeleteMapping("/{id}/leave")
    public ResponseEntity<ResponseMessage> leaveMatch(@PathVariable UUID id,
                                                      @AuthenticationPrincipal UserPrincipal user) {
        matchManagement.leaveMatch(id, user.getUserTelegramId());
        return ResponseEntity.ok(ResponseMessage.success("Muvaffaqiyatli tark etildi", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseMessage> getMatch(@PathVariable UUID id) {
        var data = matchManagement.getMatch(id);
        return ResponseEntity.ok(ResponseMessage.success("Muvaffaqiyatli", data));
    }

}
