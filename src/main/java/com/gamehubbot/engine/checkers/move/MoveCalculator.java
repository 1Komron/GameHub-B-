package com.gamehubbot.engine.checkers.move;

import com.gamehubbot.engine.checkers.CheckersState;
import com.gamehubbot.engine.checkers.piece.Piece;
import com.gamehubbot.engine.checkers.piece.PieceColor;
import com.gamehubbot.engine.checkers.piece.PieceType;
import com.gamehubbot.engine.checkers.cell.Cell;
import com.gamehubbot.engine.checkers.cell.CellPosition;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MoveCalculator {

    private static final int[][] DIAGONAL_DIRS = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

    public List<PossibleMove> getMovesForCell(CheckersState state, CellPosition pos) {
        Cell cell = state.getCells()[pos.row()][pos.col()];
        System.out.println("cell = " + cell);
        Piece piece = cell.getPiece();
        if (piece == null) return List.of();

        // Сначала проверяем — есть ли ОБЯЗАТЕЛЬНЫЕ взятия у ЛЮБОЙ фишки этого цвета
        List<PossibleMove> allCaptures = getAllCapturesForColor(state, piece.getColor());

        if (!allCaptures.isEmpty()) {
            // Обязаны бить — возвращаем только те взятия, что начинаются с этой клетки
            return allCaptures.stream()
                    .filter(m -> m.from().equals(pos))
                    .toList();
        }

        // Взятий нет нигде — обычные ходы для этой фишки
        return getSimpleMoves(state, pos, piece);
    }

    // ---- Обычные ходы (без взятия) ----

    private List<PossibleMove> getSimpleMoves(CheckersState state, CellPosition pos, Piece piece) {
        List<PossibleMove> moves = new ArrayList<>();

        if (piece.getType() == PieceType.KING) {
            moves.addAll(getKingSimpleMoves(state, pos));
        } else {
            moves.addAll(getPawnSimpleMoves(state, pos, piece));
        }

        return moves;
    }

    private List<PossibleMove> getPawnSimpleMoves(CheckersState state, CellPosition pos, Piece piece) {
        List<PossibleMove> moves = new ArrayList<>();
        int forward = piece.getColor() == PieceColor.BLACK ? 1 : -1;

        int[][] pawnDirs = {{forward, -1}, {forward, 1}};
        for (int[] dir : pawnDirs) {
            CellPosition to = new CellPosition(pos.row() + dir[0], pos.col() + dir[1]);
            if (to.isOnBoard() && state.getCells()[to.row()][to.col()].getPiece() == null) {
                moves.add(new PossibleMove(pos, to, false, List.of()));
            }
        }
        return moves;
    }

    private List<PossibleMove> getKingSimpleMoves(CheckersState state, CellPosition pos) {
        List<PossibleMove> moves = new ArrayList<>();

        for (int[] dir : DIAGONAL_DIRS) {
            int r = pos.row() + dir[0];
            int c = pos.col() + dir[1];

            // "летающая" дамка — идём пока клетки свободны
            while (new CellPosition(r, c).isOnBoard() && state.getCells()[r][c].getPiece() == null) {
                moves.add(new PossibleMove(pos, new CellPosition(r, c), false, List.of()));
                r += dir[0];
                c += dir[1];
            }
        }
        return moves;
    }

    // ---- Взятия (с рекурсивной цепочкой multi-jump) ----

    public List<PossibleMove> getAllCapturesForColor(CheckersState state, PieceColor color) {
        List<PossibleMove> allCaptures = new ArrayList<>();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = state.getCells()[row][col].getPiece();
                if (piece != null && piece.getColor() == color) {
                    CellPosition from = new CellPosition(row, col);
                    allCaptures.addAll(findCaptureChains(state, from, piece, List.of()));
                }
            }
        }
        return allCaptures;
    }

    private List<PossibleMove> findCaptureChains(CheckersState state, CellPosition from,
                                                 Piece piece, List<CellPosition> alreadyCaptured) {
        List<PossibleMove> chains = new ArrayList<>();

        List<PossibleMove> immediateCaptures = piece.getType() == PieceType.KING
                ? getKingCaptures(state, from, piece, alreadyCaptured)
                : getPawnCaptures(state, from, piece, alreadyCaptured);

        for (PossibleMove capture : immediateCaptures) {
            List<CellPosition> newCaptured = new ArrayList<>(alreadyCaptured);
            newCaptured.addAll(capture.capturedPieces());

            // проверяем, можно ли продолжить бить с новой позиции
            List<PossibleMove> continuation = findCaptureChains(state, capture.to(), piece, newCaptured);

            if (continuation.isEmpty()) {
                // цепочка закончилась здесь
                chains.add(new PossibleMove(from, capture.to(), true, newCaptured));
            } else {
                // добавляем продолжения, но from должен остаться исходным
                for (PossibleMove cont : continuation) {
                    chains.add(new PossibleMove(from, cont.to(), true, cont.capturedPieces()));
                }
            }
        }

        return chains;
    }

    private List<PossibleMove> getPawnCaptures(CheckersState state, CellPosition from,
                                               Piece piece, List<CellPosition> alreadyCaptured) {
        List<PossibleMove> captures = new ArrayList<>();

        for (int[] dir : DIAGONAL_DIRS) {
            CellPosition enemyPos = new CellPosition(from.row() + dir[0], from.col() + dir[1]);
            CellPosition landPos = new CellPosition(from.row() + 2 * dir[0], from.col() + 2 * dir[1]);

            if (!landPos.isOnBoard() || alreadyCaptured.contains(enemyPos)) continue;

            Piece enemyPiece = enemyPos.isOnBoard() ? state.getCells()[enemyPos.row()][enemyPos.col()].getPiece() : null;
            Piece landPiece = state.getCells()[landPos.row()][landPos.col()].getPiece();

            if (enemyPiece != null && enemyPiece.getColor() != piece.getColor() && landPiece == null) {
                captures.add(new PossibleMove(from, landPos, true, List.of(enemyPos)));
            }
        }
        return captures;
    }

    private List<PossibleMove> getKingCaptures(CheckersState state, CellPosition from,
                                               Piece piece, List<CellPosition> alreadyCaptured) {
        List<PossibleMove> captures = new ArrayList<>();

        for (int[] dir : DIAGONAL_DIRS) {
            CellPosition enemyPos = null;
            int r = from.row() + dir[0];
            int c = from.col() + dir[1];

            // идём по диагонали пока не встретим фигуру
            while (new CellPosition(r, c).isOnBoard()) {
                Piece current = state.getCells()[r][c].getPiece();
                if (current != null) {
                    enemyPos = new CellPosition(r, c);
                    break;
                }
                r += dir[0];
                c += dir[1];
            }

            if (enemyPos == null || alreadyCaptured.contains(enemyPos)) continue;

            Piece enemyPiece = state.getCells()[enemyPos.row()][enemyPos.col()].getPiece();
            if (enemyPiece == null || enemyPiece.getColor() == piece.getColor()) continue;

            // после enemy все клетки до конца диагонали (пока свободны) — валидные приземления
            int landR = enemyPos.row() + dir[0];
            int landC = enemyPos.col() + dir[1];

            while (new CellPosition(landR, landC).isOnBoard()
                    && state.getCells()[landR][landC].getPiece() == null) {
                captures.add(new PossibleMove(from, new CellPosition(landR, landC), true, List.of(enemyPos)));
                landR += dir[0];
                landC += dir[1];
            }
        }
        return captures;
    }
}
