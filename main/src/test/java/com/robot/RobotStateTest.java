package com.robot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RobotStateTest {
    @ParameterizedTest
    @CsvSource({"N, W, E", "E, N, S", "S, E, W", "W, S, N"})
    void turnsInPlaceWithoutChangingTheOriginalState(Direction direction, Direction left, Direction right) {
        Coordinate coordinate = new Coordinate(1, 2);
        RobotState robot = new RobotState(new Position(coordinate, direction), false);

        assertEquals(new RobotState(new Position(coordinate, left), false), robot.turnLeft());
        assertEquals(new RobotState(new Position(coordinate, right), false), robot.turnRight());
        assertEquals(direction, robot.position().direction());
        assertFalse(robot.isLost());
    }

    @Test
    void lostRobotCannotTurn() {
        RobotState robot = new RobotState(new Position(new Coordinate(0, 0), Direction.N), true);

        assertEquals(robot, robot.turnLeft());
        assertEquals(robot, robot.turnRight());
    }

    @Test
    void rejectsMissingPosition() {
        assertThrows(NullPointerException.class, () -> new RobotState(null, false));
    }
}
