package com.robot;

import java.util.Objects;

/**
 * Keeps a robot's location and the direction it faces together to describe its position
 */

public record Position(Coordinate coordinate, Direction direction) {

    public Position {
        Objects.requireNonNull(coordinate, "coordinate");
        Objects.requireNonNull(direction, "direction");
    }
}
