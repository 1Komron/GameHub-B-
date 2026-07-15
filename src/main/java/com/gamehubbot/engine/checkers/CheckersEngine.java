package com.gamehubbot.engine.checkers;

import com.gamehubbot.engine.GameEngine;
import com.gamehubbot.engine.GameResult;
import com.gamehubbot.engine.checkers.cell.Cell;
import com.gamehubbot.engine.checkers.cell.CellPosition;
import com.gamehubbot.engine.checkers.dto.request.MoveRequest;
import com.gamehubbot.engine.checkers.move.MoveCalculator;
import com.gamehubbot.engine.checkers.move.MoveCheckers;
import com.gamehubbot.engine.checkers.move.PossibleMove;
import com.gamehubbot.engine.checkers.piece.Piece;
import com.gamehubbot.engine.checkers.piece.PieceColor;
import com.gamehubbot.engine.tictactoe.MoveCommand;
import com.gamehubbot.match.domain.entity.MatchPlayer;
import com.gamehubbot.match.domain.entity.MatchState;
import com.gamehubbot.match.infrastructure.repository.MatchPlayerRepository;
import com.gamehubbot.match.infrastructure.repository.MatchStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CheckersEngine extends CheckersBase implements GameEngine {
    private final MoveCalculator moveCalculator;
    private final MatchStateRepository stateRepository;
    private final ObjectMapper objectMapper;
    private final MatchPlayerRepository playerRepository;

    @Override
    public Object applyMove(Object stateObj, MoveCommand move) {
        CheckersState state = (CheckersState) stateObj;
        MoveRequest moveRequest = ((MoveCheckers) move).moveRequest();
        CellPosition from = moveRequest.from();
        CellPosition to = moveRequest.to();

        List<PossibleMove> legalMoves = moveCalculator.getMovesForCell(state, from);
        System.out.println("to = " + to);
        System.out.println("legalMoves.size() = " + legalMoves.size());
        legalMoves.forEach(possibleMove -> {
            System.out.println("possibleMove = " + possibleMove.toString());
        });
        PossibleMove chosen = legalMoves.stream()
                .filter(m -> m.to().equals(to))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Illegal move"));

        Cell fromCell = state.getCell(from);
        Cell toCell = state.getCell(to);
        Piece piece = fromCell.getPiece();

        // 1. снять все взятые фигуры (chosen уже содержит ПОЛНУЮ цепочку взятий,
        //    т.к. findCaptureChains строит multi-jump сразу целиком)
        for (CellPosition captured : chosen.capturedPieces()) {
            state.getCell(captured).clear();
        }

        // 2. переместить фигуру
        fromCell.clear();
        toCell.move(piece);

        // 3. промоушен в дамку (если ещё не дамка и дошла до последней горизонтали)
        int promotionRow = piece.getColor() == PieceColor.WHITE ? 0 : 7;
        if (to.row() == promotionRow && !piece.isKing()) {
            toCell.move(piece.promoteToKing());
        }

        // 4. смена хода
        state.switchTurn();

        return state;
    }

    @Override
    public GameResult evaluate(Object stateObj) {
        CheckersState state = (CheckersState) stateObj;
        PieceColor turn = state.getTurn();

        boolean hasPieces = false;
        boolean hasMove = false;

        Cell[][] cells = state.getCells();
        for (int row = 0; row < cells.length; row++) {
            for (int col = 0; col < cells[row].length; col++) {
                Cell cell = cells[row][col];
                Piece piece = cell.getPiece();
                if (piece != null && piece.getColor() == turn) {
                    hasPieces = true;
                    List<PossibleMove> moves = moveCalculator.getMovesForCell(state, new CellPosition(row, col));
                    if (!moves.isEmpty()) {
                        hasMove = true;
                        break;
                    }
                }
            }
            if (hasMove) break;
        }

        if (!hasPieces || !hasMove) {
            // у игрока на ходу нет фигур или ходов — он проигрывает
            int loserSeat = state.seatForColor(turn);
            int winnerSeat = loserSeat == 0 ? 1 : 0;
            return GameResult.win(winnerSeat);
        }

        return GameResult.ongoing();
    }

    public List<PossibleMove> getPossibleMoves(UUID matchId, Long userId, CellPosition pos) {
        MatchState matchState = stateRepository.findById(matchId)
                .orElseThrow(() -> new NoSuchElementException("Match state not found"));
        CheckersState state = objectMapper.readValue(matchState.getStateJson(), CheckersState.class);

        if (!pos.isOnBoard()) {
            return List.of();
        }

        Cell cell = state.getCells()[pos.row()][pos.col()];
        Piece piece = cell.getPiece();

        if (piece == null) {
            return List.of();
        }

        MatchPlayer player = playerRepository.findByMatchIdAndUserId(matchId, userId)
                .orElseThrow(() -> new NoSuchElementException("Player not found"));

        // кликнули по чужой фигуре — не отдаём ходы (иначе игрок увидит ходы соперника)
        if (piece.getColor() != state.colorForPlayer(player.getSeat())) {
            return List.of();
        }

        // не свой ход — тоже не отдаём (даже по своей фигуре нет доступных ходов сейчас)
        if (!state.checkTurn(player.getSeat())) {
            return List.of();
        }

        return moveCalculator.getMovesForCell(state, pos);
    }
}
