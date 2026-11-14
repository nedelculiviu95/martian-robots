package com.robot;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CoordinateTest {
    @Test
    void storesCoordinatesIncludingAnOffWorldCandidate() {
        Coordinate coordinate = new Coordinate(-1, AppConstraints.MAX_COORDINATE + 1);

        assertEquals(-1, coordinate.x());
        assertEquals(AppConstraints.MAX_COORDINATE + 1, coordinate.y());
    }

    @Test
    void equalCoordinatesIdentifyTheSameScentLocation() {
        var scents = new HashSet<>(List.of(new Coordinate(1, 2), new Coordinate(1, 2)));

        assertEquals(1, scents.size());
        assertEquals(new Coordinate(1, 2), scents.iterator().next());
    }
}
