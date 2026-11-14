package com.robot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static com.robot.Direction.E;
import static com.robot.Direction.N;
import static com.robot.Direction.S;
import static com.robot.Direction.W;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorldTest {
    private final World world = new World(new GridBounds(2, 2));

    @Test
    void rejectsNullBounds() {
        assertThrows(NullPointerException.class, () -> new World(null));
    }

    @Test
    void rejectsNullRobot() {
        assertThrows(NullPointerException.class, () -> world.moveRobot(null, N));
    }

    @Test
    void rejectsNullMovementDirection() {
        assertThrows(NullPointerException.class, () -> world.moveRobot(alive(1, 1, N), null));
    }

    @Test
    void leavesALostRobotUnchanged() {
        RobotState robot = lost(1, 1, N);

        assertSame(robot, world.moveRobot(robot, E));
    }

    @ParameterizedTest
    @CsvSource({"-1, 1", "3, 1", "1, -1", "1, 3"})
    void rejectsALiveRobotOutsideTheWorld(int x, int y) {
        assertThrows(IllegalArgumentException.class, () -> world.moveRobot(alive(x, y, N), N));
    }

    @ParameterizedTest
    @CsvSource({"N, 1, 2", "E, 2, 1", "S, 1, 0", "W, 0, 1"})
    void movesOneCellWithoutChangingTheRobotsFacing(Direction movement, int x, int y) {
        RobotState result = world.moveRobot(alive(1, 1, N), movement);

        assertEquals(alive(x, y, N), result);
    }

    @ParameterizedTest
    @CsvSource({"1, 2, N", "2, 1, E", "1, 0, S", "0, 1, W"})
    void reportsTheLastValidPositionWhenLeavingAnEdge(int x, int y, Direction direction) {
        assertEquals(lost(x, y, direction), world.moveRobot(alive(x, y, direction), direction));
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    void everyMoveLeavesASingleCellWorld(Direction direction) {
        World singleCell = new World(new GridBounds(0, 0));

        assertEquals(lost(0, 0, direction), singleCell.moveRobot(alive(0, 0, direction), direction));
    }

    @Test
    void ignoresALaterExitFromTheSameCoordinate() {
        world.moveRobot(alive(0, 2, N), N);
        RobotState protectedRobot = alive(0, 2, N);

        assertSame(protectedRobot, world.moveRobot(protectedRobot, N));
    }

    @Test
    void allowsMovementFromAScentedCoordinateIntoTheWorld() {
        world.moveRobot(alive(0, 2, N), N);

        assertEquals(alive(1, 2, E), world.moveRobot(alive(0, 2, E), E));
    }

    @Test
    void allowsMovementIntoAScentedCoordinate() {
        world.moveRobot(alive(0, 2, N), N);

        assertEquals(alive(0, 2, N), world.moveRobot(alive(0, 1, N), N));
    }

    @Test
    void cornerScentProtectsBothOutwardDirections() {
        world.moveRobot(alive(0, 0, W), W);
        RobotState southFacingRobot = alive(0, 0, S);

        assertSame(southFacingRobot, world.moveRobot(southFacingRobot, S));
    }

    @Test
    void scentDoesNotProtectADifferentCoordinate() {
        world.moveRobot(alive(0, 2, N), N);

        assertEquals(lost(1, 2, N), world.moveRobot(alive(1, 2, N), N));
    }

    @Test
    void scentsAreNotSharedBetweenWorlds() {
        world.moveRobot(alive(0, 2, N), N);
        World otherWorld = new World(new GridBounds(2, 2));

        assertEquals(lost(0, 2, N), otherWorld.moveRobot(alive(0, 2, N), N));
    }

    private static RobotState alive(int x, int y, Direction direction) {
        return new RobotState(new Position(new Coordinate(x, y), direction), false);
    }
    private static RobotState lost(int x, int y, Direction direction) {
        return new RobotState(new Position(new Coordinate(x, y), direction), true);
    }
}
