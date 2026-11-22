package com.example.checkers.game;

import java.util.Objects;

public final class Piece {
    private final Side side;
    private final boolean king;

    public Piece(Side side, boolean king) {
        this.side = side;
        this.king = king;
    }

    public static Piece man(Side side) {
        return new Piece(side, false);
    }

    public static Piece king(Side side) {
        return new Piece(side, true);
    }

    public Side getSide() {
        return side;
    }

    public boolean isKing() {
        return king;
    }

    public Piece crowned() {
        return king ? this : new Piece(side, true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Piece)) return false;
        Piece other = (Piece) o;
        return side == other.side && king == other.king;
    }

    @Override
    public int hashCode() {
        return Objects.hash(side, king);
    }

    @Override
    public String toString() {
        return side + (king ? " king" : " man");
    }
}
