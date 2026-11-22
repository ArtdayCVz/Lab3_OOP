package com.example.checkers.game;

public enum Difficulty {
    EASY(2),
    MEDIUM(4),
    HARD(6);

    private final int depth;

    Difficulty(int depth) {
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }
}
