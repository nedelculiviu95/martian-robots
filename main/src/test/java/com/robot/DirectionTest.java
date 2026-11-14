package com.robot;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectionTest {
    @ParameterizedTest
    @CsvSource({
            "N, 0, 1",
            "E, 1, 0",
            "S, 0, -1",
            "W, -1, 0"
    })
    void hasExpectedMovement(Direction direction, int expectedDx, int expectedDy) {
        assertAll(
                () -> assertEquals(expectedDx, direction.dx(), "x displacement"),
                () -> assertEquals(expectedDy, direction.dy(), "y displacement")
        );
    }

    @ParameterizedTest
    @CsvSource({
            "N, W",
            "E, N",
            "S, E",
            "W, S"
    })
    void turnsLeft(Direction direction, Direction expected) {
        assertEquals(expected, direction.left());
    }

    @ParameterizedTest
    @CsvSource({
            "N, E",
            "E, S",
            "S, W",
            "W, N"
    })
    void turnsRight(Direction direction, Direction expected) {
        assertEquals(expected, direction.right());
    }

    @ParameterizedTest
    @CsvSource({
            "N, S",
            "E, W",
            "S, N",
            "W, E"
    })
    void findsOppositeDirection(Direction direction, Direction expected) {
        assertEquals(expected, direction.opposite());
    }
}
