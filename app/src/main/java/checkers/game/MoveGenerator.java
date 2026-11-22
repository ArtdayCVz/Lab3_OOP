package checkers.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MoveGenerator {

    private static final Pos[] DIRECTIONS = {
            new Pos(-1, -1), new Pos(-1, 1), new Pos(1, -1), new Pos(1, 1)
    };

    private MoveGenerator() {
    }

    private static final class Jump {
        final Pos victim;
        final Pos landing;

        Jump(Pos victim, Pos landing) {
            this.victim = victim;
            this.landing = landing;
        }
    }

    public static List<Move> legalMoves(Board board, Side side) {
        List<Board.Placed> pieces = board.pieces(side);

        List<Move> captures = new ArrayList<>();
        for (Board.Placed placed : pieces) {
            collectCaptures(board.without(placed.pos), placed.pos, placed.piece,
                    Collections.<Pos>emptyList(), Collections.singletonList(placed.pos), captures);
        }
        if (!captures.isEmpty()) return captures;

        List<Move> quiet = new ArrayList<>();
        for (Board.Placed placed : pieces) addQuietMoves(board, placed, quiet);
        return quiet;
    }

    private static void addQuietMoves(Board board, Board.Placed placed, List<Move> out) {
        Pos pos = placed.pos;
        if (placed.piece.isKing()) {
            for (Pos d : DIRECTIONS) {
                Pos p = pos.plus(d);
                while (p.inBounds() && board.isEmpty(p)) {
                    out.add(new Move(listOf(pos, p)));
                    p = p.plus(d);
                }
            }
        } else {
            for (int dc = -1; dc <= 1; dc += 2) {
                Pos p = new Pos(pos.row + placed.piece.getSide().forward(), pos.col + dc);
                if (p.inBounds() && board.isEmpty(p)) out.add(new Move(listOf(pos, p)));
            }
        }
    }

    private static Piece promotedIfOnLastRank(Piece piece, Pos landing) {
        return !piece.isKing() && landing.row == piece.getSide().promotionRow() ? piece.crowned() : piece;
    }

    private static void collectCaptures(Board board, Pos pos, Piece piece, List<Pos> captured,
                                        List<Pos> path, List<Move> out) {
        List<Jump> jumps = jumpsFrom(board, pos, piece, captured);
        if (jumps.isEmpty()) {
            if (!captured.isEmpty()) out.add(new Move(path, captured));
            return;
        }

        Map<Pos, List<Jump>> byVictim = new LinkedHashMap<>();
        for (Jump jump : jumps) {
            List<Jump> group = byVictim.get(jump.victim);
            if (group == null) {
                group = new ArrayList<>();
                byVictim.put(jump.victim, group);
            }
            group.add(jump);
        }

        for (Map.Entry<Pos, List<Jump>> entry : byVictim.entrySet()) {
            List<Pos> newCaptured = append(captured, entry.getKey());
            List<Jump> group = entry.getValue();

            List<Jump> continuing = new ArrayList<>();
            for (Jump jump : group) {
                Piece after = promotedIfOnLastRank(piece, jump.landing);
                if (!jumpsFrom(board, jump.landing, after, newCaptured).isEmpty()) continuing.add(jump);
            }

            for (Jump jump : continuing.isEmpty() ? group : continuing) {
                collectCaptures(board, jump.landing, promotedIfOnLastRank(piece, jump.landing),
                        newCaptured, append(path, jump.landing), out);
            }
        }
    }

    private static List<Jump> jumpsFrom(Board board, Pos pos, Piece piece, List<Pos> captured) {
        List<Jump> result = new ArrayList<>();
        for (Pos d : DIRECTIONS) {
            if (piece.isKing()) {
                Pos p = pos.plus(d);
                while (p.inBounds() && board.isEmpty(p)) p = p.plus(d);
                if (!p.inBounds()) continue;
                Piece target = board.get(p);
                if (target.getSide() == piece.getSide() || captured.contains(p)) continue;
                Pos landing = p.plus(d);
                while (landing.inBounds() && board.isEmpty(landing)) {
                    result.add(new Jump(p, landing));
                    landing = landing.plus(d);
                }
            } else {
                Pos victim = pos.plus(d);
                Pos landing = victim.plus(d);
                if (!landing.inBounds()) continue;
                Piece target = board.get(victim);
                if (target != null && target.getSide() != piece.getSide()
                        && !captured.contains(victim) && board.isEmpty(landing)) {
                    result.add(new Jump(victim, landing));
                }
            }
        }
        return result;
    }

    private static List<Pos> listOf(Pos a, Pos b) {
        List<Pos> list = new ArrayList<>(2);
        list.add(a);
        list.add(b);
        return list;
    }

    private static List<Pos> append(List<Pos> list, Pos pos) {
        List<Pos> copy = new ArrayList<>(list.size() + 1);
        copy.addAll(list);
        copy.add(pos);
        return copy;
    }
}
