package checkers.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ComputerPlayerTest {

    @Test
    public void choosesALegalMoveFromTheStartPosition() {
        GameState state = new GameState(Board.initial(), Side.BLACK);
        Move move = new ComputerPlayer(Difficulty.MEDIUM).chooseMove(state);
        assertTrue(state.getLegalMoves().contains(move));
    }

    @Test
    public void choosesALegalMoveForWhite() {
        GameState state = GameState.initial();
        Move move = new ComputerPlayer(Difficulty.MEDIUM).chooseMove(state);
        assertTrue(state.getLegalMoves().contains(move));
    }

    @Test
    public void returnsNullWhenThereAreNoMoves() {
        Board board = Board.empty().with(Pos.fromNotation("d4"), Piece.man(Side.WHITE));
        GameState state = new GameState(board, Side.BLACK);
        assertNull(new ComputerPlayer(Difficulty.EASY).chooseMove(state));
    }

    @Test
    public void capturesWhenCaptureIsMandatory() {
        Board board = Board.empty()
                .with(Pos.fromNotation("a7"), Piece.man(Side.BLACK))
                .with(Pos.fromNotation("b6"), Piece.man(Side.WHITE));
        Move move = new ComputerPlayer(Difficulty.HARD).chooseMove(new GameState(board, Side.BLACK));
        assertEquals("a7:c5", move.toString());
    }
}
