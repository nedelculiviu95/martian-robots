package com.robot;

/**
 * Represents north, east, south and west so robots can turn and move the right way
 */

public enum Direction {
    N(0, 1), E(1, 0), S(0, -1), W(-1, 0);

    private final int dx;
    private final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int dx() {
        return dx;
    }

    public int dy() {
        return dy;
    }

    public Direction left() {
        return switch (this) {
            case N -> W;
            case E -> N;
            case S -> E;
            case W -> S;
        };
    }

    public Direction right() {
        return switch (this) {
            case N -> E;
            case E -> S;
            case S -> W;
            case W -> N;
        };
    }

    public Direction opposite() {
        return right().right();
    }

}
