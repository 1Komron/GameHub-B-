package com.gamehubbot.match.api;

import com.gamehubbot.common.ResponseMessage;
import com.gamehubbot.engine.checkers.application.CheckersManagement;
import com.gamehubbot.engine.checkers.cell.CellPosition;
import com.gamehubbot.engine.checkers.dto.request.MoveRequest;
import com.gamehubbot.user.domain.entity.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CheckersController {
    private final CheckersManagement management;

    @GetMapping("/api/checkers/{matchId}/possible-moves")
    public ResponseEntity<ResponseMessage> getPossibleMoves(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID matchId,
            @RequestParam int row,
            @RequestParam int col) {
        var data = management.getPossibleMoves(matchId, principal.getUserTelegramId(), new CellPosition(row, col));
        return ResponseEntity.ok(new ResponseMessage(true, "Possible moves", data));
    }

    @PostMapping("/api/checkers/{matchId}/move")
    public ResponseEntity<ResponseMessage> move(
            @PathVariable UUID matchId,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody MoveRequest moveRequest) {
        var data = management.move(matchId, principal.getUserTelegramId(), moveRequest);
        return ResponseEntity.ok(new ResponseMessage(true, "Moved", data));
    }
}