package com.example.checkers.ui;

import com.example.checkers.game.Board;
import com.example.checkers.game.Pos;
import java.util.List;
import java.util.Set;

public final class GameUiState {
    public final Board board;
    public final Status status;
    public final List<Pos> selectedPath;
    public final Set<Pos> targets;
    public final Set<Pos> hintPieces;
    public final List<Pos> lastMovePath;
    public final int humanPieces;
    public final int computerPieces;

    public GameUiState(Board board, Status status, List<Pos> selectedPath, Set<Pos> targets,
                       Set<Pos> hintPieces, List<Pos> lastMovePath, int humanPieces, int computerPieces) {
        this.board = board;
        this.status = status;
        this.selectedPath = selectedPath;
        this.targets = targets;
        this.hintPieces = hintPieces;
        this.lastMovePath = lastMovePath;
        this.humanPieces = humanPieces;
        this.computerPieces = computerPieces;
    }
}
