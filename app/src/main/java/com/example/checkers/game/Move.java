package com.example.checkers.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Move {
    private final List<Pos> path;
    private final List<Pos> captured;

    public Move(List<Pos> path, List<Pos> captured) {
        this.path = Collections.unmodifiableList(new ArrayList<>(path));
        this.captured = Collections.unmodifiableList(new ArrayList<>(captured));
    }

    public Move(List<Pos> path) {
        this(path, Collections.<Pos>emptyList());
    }

    public List<Pos> getPath() {
        return path;
    }

    public List<Pos> getCaptured() {
        return captured;
    }

    public Pos getFrom() {
        return path.get(0);
    }

    public Pos getTo() {
        return path.get(path.size() - 1);
    }

    public boolean isCapture() {
        return !captured.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Move)) return false;
        Move other = (Move) o;
        return path.equals(other.path) && captured.equals(other.captured);
    }

    @Override
    public int hashCode() {
        return path.hashCode() * 31 + captured.hashCode();
    }

    @Override
    public String toString() {
        String separator = isCapture() ? ":" : "-";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) sb.append(separator);
            sb.append(path.get(i));
        }
        return sb.toString();
    }
}
