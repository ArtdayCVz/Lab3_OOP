package checkers.game;

public final class Pos {
    public final int row;
    public final int col;

    public Pos(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public Pos plus(Pos delta) {
        return new Pos(row + delta.row, col + delta.col);
    }

    public boolean inBounds() {
        return row >= 0 && row < Board.SIZE && col >= 0 && col < Board.SIZE;
    }

    public boolean isDark() {
        return (row + col) % 2 == 1;
    }

    public static Pos fromNotation(String text) {
        if (text == null || text.length() != 2) {
            throw new IllegalArgumentException("Bad square: " + text);
        }
        Pos pos = new Pos(Board.SIZE - (text.charAt(1) - '0'), text.charAt(0) - 'a');
        if (!pos.inBounds()) {
            throw new IllegalArgumentException("Bad square: " + text);
        }
        return pos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pos)) return false;
        Pos other = (Pos) o;
        return row == other.row && col == other.col;
    }

    @Override
    public int hashCode() {
        return row * 31 + col;
    }

    @Override
    public String toString() {
        return "" + (char) ('a' + col) + (Board.SIZE - row);
    }
}
