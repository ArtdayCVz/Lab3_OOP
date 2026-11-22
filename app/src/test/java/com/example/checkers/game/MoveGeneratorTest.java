package com.example.checkers.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class MoveGeneratorTest {

    private static Pos p(String text) {
        return Pos.fromNotation(text);
    }

    private static Board board(Object... placements) {
        Board board = Board.empty();
        for (int i = 0; i < placements.length; i += 2) {
            board = board.with(p((String) placements[i]), (Piece) placements[i + 1]);
        }
        return board;
    }

    private static Piece white() { return Piece.man(Side.WHITE); }
    private static Piece whiteKing() { return Piece.king(Side.WHITE); }
    private static Piece black() { return Piece.man(Side.BLACK); }

    private static Set<String> names(List<Move> moves) {
        Set<String> result = new HashSet<>();
        for (Move move : moves) result.add(move.toString());
        return result;
    }

    @Test
    public void notationRoundTrip() {
        assertEquals("c3", p("c3").toString());
        assertEquals(new Pos(7, 0), p("a1"));
        assertEquals(new Pos(0, 7), p("h8"));
    }

    @Test
    public void initialPositionHasTwelveMenPerSideAndSevenWhiteMoves() {
        Board board = Board.initial();
        assertEquals(12, board.count(Side.WHITE));
        assertEquals(12, board.count(Side.BLACK));
        assertEquals(7, MoveGenerator.legalMoves(board, Side.WHITE).size());
    }

    @Test
    public void manCapturesBackwardsAndCaptureIsMandatory() {
        Board board = board("d4", white(), "e3", black(), "a1", white());
        List<Move> moves = MoveGenerator.legalMoves(board, Side.WHITE);
        assertEquals(new HashSet<>(Arrays.asList("d4:f2")), names(moves));
        assertEquals(1, moves.size());
    }

    @Test
    public void flyingKingCanLandOnAnySquareBehindVictim() {
        Board board = board("a1", whiteKing(), "d4", black());
        List<Move> moves = MoveGenerator.legalMoves(board, Side.WHITE);
        assertEquals(4, moves.size());
        for (Move move : moves) {
            assertEquals(Arrays.asList(p("d4")), move.getCaptured());
        }
    }

    @Test
    public void kingMustContinueCapturing() {
        Board board = board("a1", whiteKing(), "d4", black(), "f6", black());
        List<Move> moves = MoveGenerator.legalMoves(board, Side.WHITE);
        assertEquals(new HashSet<>(Arrays.asList("a1:e5:g7", "a1:e5:h8")), names(moves));
        for (Move move : moves) assertEquals(2, move.getCaptured().size());
    }

    @Test
    public void manPromotedDuringCaptureContinuesAsKing() {
        Board board = board("b6", white(), "c7", black(), "f6", black());
        List<Move> moves = MoveGenerator.legalMoves(board, Side.WHITE);
        assertEquals(new HashSet<>(Arrays.asList("b6:d8:g5", "b6:d8:h4")), names(moves));
    }

    @Test
    public void manIsPromotedOnLastRank() {
        Board board = board("a7", white());
        List<Move> moves = MoveGenerator.legalMoves(board, Side.WHITE);
        assertEquals(1, moves.size());
        assertEquals("a7-b8", moves.get(0).toString());
        assertEquals(whiteKing(), board.apply(moves.get(0)).get(p("b8")));
    }

    @Test
    public void capturedPiecesAreRemovedAfterMove() {
        Board board = board("d4", white(), "e3", black());
        Board after = board.apply(MoveGenerator.legalMoves(board, Side.WHITE).get(0));
        assertEquals(0, after.count(Side.BLACK));
        assertEquals(white(), after.get(p("f2")));
    }

    @Test
    public void sideWithoutMovesLoses() {
        GameState state = new GameState(board("d4", white()), Side.BLACK);
        assertEquals(Outcome.WHITE_WINS, state.getOutcome());
    }

    @Test
    public void gameIsOngoingAtStart() {
        assertTrue(GameState.initial().getOutcome() == Outcome.ONGOING);
    }
}
