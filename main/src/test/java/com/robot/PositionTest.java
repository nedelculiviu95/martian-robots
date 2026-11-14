package com.robot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PositionTest {
    @Test
    void storesCoordinateAndDirection() {
        Coordinate coordinate = new Coordinate(1, 2);
        Position position = new Position(coordinate, Direction.W);

        assertEquals(coordinate, position.coordinate());
        assertEquals(Direction.W, position.direction());
    }

    @Test
    void rejectsMissingCoordinate() {
        assertThrows(NullPointerException.class, () -> new Position(null, Direction.N));
    }

    @Test
    void rejectsMissingDirection() {
        assertThrows(NullPointerException.class, () -> new Position(new Coordinate(0, 0), null));
    }
}
