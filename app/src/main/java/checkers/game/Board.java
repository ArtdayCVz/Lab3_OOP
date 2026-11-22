package checkers.game;

import java.util.ArrayList;
import java.util.List;

public final class Board {
    public static final int SIZE = 8;

    public static final class Placed {
        public final Pos pos;
        public final Piece piece;

        Placed(Pos pos, Piece piece) {
            this.pos = pos;
            this.piece = piece;
        }
    }

    private final Piece[] cells;

    private Board(Piece[] cells) {
        this.cells = cells;
    }

    private static int index(Pos pos) {
        return pos.row * SIZE + pos.col;
    }

    public static Board empty() {
        return new Board(new Piece[SIZE * SIZE]);
    }

    public static Board initial() {
        Piece[] cells = new Piece[SIZE * SIZE];
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Pos pos = new Pos(row, col);
                if (!pos.isDark()) continue;
                if (row < 3) {
                    cells[index(pos)] = Piece.man(Side.BLACK);
                } else if (row > 4) {
                    cells[index(pos)] = Piece.man(Side.WHITE);
                }
            }
        }
        return new Board(cells);
    }

    public Piece get(Pos pos) {
        return cells[index(pos)];
    }

    public boolean isEmpty(Pos pos) {
        return get(pos) == null;
    }

    public List<Placed> allPieces() {
        List<Placed> result = new ArrayList<>(24);
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Pos pos = new Pos(row, col);
                Piece piece = get(pos);
                if (piece != null) result.add(new Placed(pos, piece));
            }
        }
        return result;
    }

    public List<Placed> pieces(Side side) {
        List<Placed> result = new ArrayList<>(12);
        for (Placed placed : allPieces()) {
            if (placed.piece.getSide() == side) result.add(placed);
        }
        return result;
    }

    public int count(Side side) {
        int n = 0;
        for (Piece piece : cells) {
            if (piece != null && piece.getSide() == side) n++;
        }
        return n;
    }

    public Board without(Pos pos) {
        Piece[] copy = cells.clone();
        copy[index(pos)] = null;
        return new Board(copy);
    }

    public Board with(Pos pos, Piece piece) {
        Piece[] copy = cells.clone();
        copy[index(pos)] = piece;
        return new Board(copy);
    }

    public Board apply(Move move) {
        Piece piece = get(move.getFrom());
        if (piece == null) {
            throw new IllegalArgumentException("No piece on " + move.getFrom());
        }
        Piece[] copy = cells.clone();
        copy[index(move.getFrom())] = null;
        for (Pos victim : move.getCaptured()) copy[index(victim)] = null;

        boolean promoted = piece.isKing();
        for (Pos step : move.getPath()) {
            if (step.row == piece.getSide().promotionRow()) promoted = true;
        }
        copy[index(move.getTo())] = promoted ? piece.crowned() : piece;
        return new Board(copy);
    }
}
