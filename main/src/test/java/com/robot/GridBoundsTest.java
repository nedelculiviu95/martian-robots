package com.robot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GridBoundsTest {

    @ParameterizedTest
    @ValueSource(ints = {AppConstraints.MIN_COORDINATE, AppConstraints.MAX_COORDINATE})
    void acceptsInclusiveCoordinateLimits(int coordinate) {
        GridBounds bounds = new GridBounds(coordinate, coordinate);

        assertEquals(coordinate, bounds.maxX());
        assertEquals(coordinate, bounds.maxY());
        assertTrue(GridBounds.isValidCoordinate(coordinate));
    }

    @ParameterizedTest
    @ValueSource(ints = {AppConstraints.MIN_COORDINATE - 1, AppConstraints.MAX_COORDINATE + 1})
    void rejectsEitherMaximumOutsideTheAllowedRange(int coordinate) {
        assertFalse(GridBounds.isValidCoordinate(coordinate));
        assertThrows(IllegalArgumentException.class, () -> new GridBounds(coordinate, AppConstraints.MIN_COORDINATE));
        assertThrows(IllegalArgumentException.class, () -> new GridBounds(AppConstraints.MIN_COORDINATE, coordinate));
    }

    @ParameterizedTest
    @CsvSource({"0, 0, true", "2, 3, true", "0, 3, true", "2, 0, true", "1, 2, true",
            "-1, 1, false", "3, 1, false", "1, -1, false", "1, 4, false"})
    void containsOnlyCoordinatesInsideItsInclusiveEdges(int x, int y, boolean expected) {
        assertEquals(expected, new GridBounds(2, 3).contains(new Coordinate(x, y)));
    }

    @Test
    void zeroByZeroWorldContainsOnlyTheOrigin() {
        GridBounds bounds = new GridBounds(0, 0);

        assertTrue(bounds.contains(new Coordinate(0, 0)));
        assertFalse(bounds.contains(new Coordinate(1, 0)));
        assertFalse(bounds.contains(new Coordinate(0, 1)));
    }

    @Test
    void rejectsMissingCoordinate() {
        assertThrows(NullPointerException.class, () -> new GridBounds(1, 1).contains(null));
    }
}
