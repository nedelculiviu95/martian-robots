package com.robot;

/**
 * Defines the edges of the grid so the simulation can check whether a square is inside it.
 */
public record GridBounds(int maxX, int maxY) {

    public GridBounds {
        if (!isValidCoordinate(maxX) || !isValidCoordinate(maxY)) {
            throw new IllegalArgumentException("World gridBounds must be between " + AppConstraints.MIN_COORDINATE + " and " + AppConstraints.MAX_COORDINATE);
        }
    }

    public static boolean isValidCoordinate(int coordinate) {
        return coordinate >= AppConstraints.MIN_COORDINATE && coordinate <= AppConstraints.MAX_COORDINATE;
    }

    public boolean contains(Coordinate coordinate) {
        return coordinate.x() >= AppConstraints.MIN_COORDINATE
                && coordinate.x() <= maxX
                && coordinate.y() >= AppConstraints.MIN_COORDINATE
                && coordinate.y() <= maxY;
    }

}
