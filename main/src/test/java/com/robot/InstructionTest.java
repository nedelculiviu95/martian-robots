package com.robot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static com.robot.Direction.N;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InstructionTest {
    private final World world = new World(new GridBounds(2, 2));

    @Test
    void declaresTheExpectedInputCharacters() {
        assertAll(
                () -> assertEquals('L', TurnLeft.SYMBOL),
                () -> assertEquals('R', TurnRight.SYMBOL),
                () -> assertEquals('F', MoveForward.SYMBOL)
        );
    }

    @ParameterizedTest
    @CsvSource({"N, W", "E, N", "S, E", "W, S"})
    void turnsLeftWithoutMoving(Direction facing, Direction expected) {
        RobotState result = new TurnLeft().execute(alive(1, 1, facing), world);

        assertEquals(alive(1, 1, expected), result);
    }

    @ParameterizedTest
    @CsvSource({"N, E", "E, S", "S, W", "W, N"})
    void turnsRightWithoutMoving(Direction facing, Direction expected) {
        RobotState result = new TurnRight().execute(alive(1, 1, facing), world);

        assertEquals(alive(1, 1, expected), result);
    }

    @ParameterizedTest
    @CsvSource({"N, 1, 2", "E, 2, 1", "S, 1, 0", "W, 0, 1"})
    void movesForwardInTheRobotsFacingDirection(Direction facing, int x, int y) {
        RobotState result = new MoveForward().execute(alive(1, 1, facing), world);

        assertEquals(alive(x, y, facing), result);
    }

    @Test
    void allInstructionsLeaveALostRobotUnchanged() {
        RobotState lostRobot = new RobotState(alive(1, 1, N).position(), true);

        for (Instruction instruction : List.of(new TurnLeft(), new TurnRight(), new MoveForward())) {
            assertSame(lostRobot, instruction.execute(lostRobot, world));
        }
    }

    @Test
    void movingForwardCanLoseTheRobot() {
        RobotState robot = alive(1, 2, N);

        assertEquals(new RobotState(robot.position(), true), new MoveForward().execute(robot, world));
    }

    @Test
    void movingForwardUsesScentProtection() {
        MoveForward forward = new MoveForward();
        RobotState robot = alive(1, 2, N);
        forward.execute(robot, world);

        assertSame(robot, forward.execute(robot, world));
    }

    @Test
    void instructionsRejectANullRobot() {
        for (Instruction instruction : List.of(new TurnLeft(), new TurnRight(), new MoveForward())) {
            assertThrows(NullPointerException.class, () -> instruction.execute(null, world));
        }
    }

    @Test
    void movingForwardRequiresAWorld() {
        assertThrows(NullPointerException.class, () -> new MoveForward().execute(alive(1, 1, N), null));
    }

    private static RobotState alive(int x, int y, Direction direction) {
        return new RobotState(new Position(new Coordinate(x, y), direction), false);
    }
}
