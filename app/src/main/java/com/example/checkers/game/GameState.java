package com.example.checkers.game;

import java.util.List;

public final class GameState {
    public static final int DRAW_AFTER_QUIET_PLIES = 30;

    private final Board board;
    private final Side turn;
    private final int quietPlies;
    private List<Move> legalMoves;

    public GameState(Board board, Side turn, int quietPlies) {
        this.board = board;
        this.turn = turn;
        this.quietPlies = quietPlies;
    }

    public GameState(Board board, Side turn) {
        this(board, turn, 0);
    }

    public static GameState initial() {
        return new GameState(Board.initial(), Side.WHITE);
    }

    public Board getBoard() {
        return board;
    }

    public Side getTurn() {
        return turn;
    }

    public int getQuietPlies() {
        return quietPlies;
    }

    public List<Move> getLegalMoves() {
        if (legalMoves == null) legalMoves = MoveGenerator.legalMoves(board, turn);
        return legalMoves;
    }

    public Outcome getOutcome() {
        if (getLegalMoves().isEmpty()) {
            return turn == Side.WHITE ? Outcome.BLACK_WINS : Outcome.WHITE_WINS;
        }
        if (quietPlies >= DRAW_AFTER_QUIET_PLIES) return Outcome.DRAW;
        return Outcome.ONGOING;
    }

    public GameState play(Move move) {
        Piece piece = board.get(move.getFrom());
        if (piece == null) {
            throw new IllegalArgumentException("No piece on " + move.getFrom());
        }
        boolean quiet = piece.isKing() && !move.isCapture();
        return new GameState(board.apply(move), turn.opponent(), quiet ? quietPlies + 1 : 0);
    }
}
