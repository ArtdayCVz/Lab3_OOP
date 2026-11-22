package com.example.checkers.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ComputerPlayer {
    private static final int INFINITY = 1_000_000;
    private static final int WIN_SCORE = 100_000;
    private static final int MAN_VALUE = 100;
    private static final int KING_VALUE = 320;
    private static final int ADVANCE_BONUS = 6;
    private static final int CENTRE_BONUS = 4;

    private final Difficulty difficulty;
    private long nodes;

    public ComputerPlayer(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public long getNodes() {
        return nodes;
    }

    public Move chooseMove(GameState state) {
        List<Move> moves = state.getLegalMoves();
        if (moves.isEmpty()) return null;
        nodes = 0;
        if (moves.size() == 1) return moves.get(0);

        List<Move> shuffled = new ArrayList<>(moves);
        Collections.shuffle(shuffled);

        Move best = shuffled.get(0);
        int bestScore = -INFINITY;
        int alpha = -INFINITY;
        for (Move move : shuffled) {
            int score = -negamax(state.play(move), difficulty.getDepth() - 1, -INFINITY, -alpha);
            if (score > bestScore) {
                bestScore = score;
                best = move;
            }
            alpha = Math.max(alpha, score);
        }
        return best;
    }

    private int negamax(GameState state, int depth, int alpha, int beta) {
        nodes++;
        List<Move> moves = state.getLegalMoves();
        if (moves.isEmpty()) return -WIN_SCORE - depth;
        if (state.getQuietPlies() >= GameState.DRAW_AFTER_QUIET_PLIES) return 0;
        if (depth <= 0 && !moves.get(0).isCapture()) return evaluate(state.getBoard(), state.getTurn());

        int a = alpha;
        for (Move move : moves) {
            int score = -negamax(state.play(move), depth - 1, -beta, -a);
            if (score >= beta) return score;
            if (score > a) a = score;
        }
        return a;
    }

    private int evaluate(Board board, Side toMove) {
        int score = 0;
        for (Board.Placed placed : board.allPieces()) {
            Piece piece = placed.piece;
            int value;
            if (piece.isKing()) {
                value = KING_VALUE;
            } else {
                int advance = piece.getSide() == Side.WHITE ? Board.SIZE - 1 - placed.pos.row : placed.pos.row;
                value = MAN_VALUE + advance * ADVANCE_BONUS;
            }
            if (placed.pos.row >= 2 && placed.pos.row <= 5 && placed.pos.col >= 2 && placed.pos.col <= 5) {
                value += CENTRE_BONUS;
            }
            score += piece.getSide() == toMove ? value : -value;
        }
        return score;
    }
}
