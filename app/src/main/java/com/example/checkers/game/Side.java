package com.example.checkers.game;

public enum Side {
    WHITE(-1, 0),
    BLACK(1, Board.SIZE - 1);

    private final int forward;
    private final int promotionRow;

    Side(int forward, int promotionRow) {
        this.forward = forward;
        this.promotionRow = promotionRow;
    }

    public int forward() {
        return forward;
    }

    public int promotionRow() {
        return promotionRow;
    }

    public Side opponent() {
        return this == WHITE ? BLACK : WHITE;
    }
}
