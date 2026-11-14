package com.robot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static com.robot.Direction.E;
import static com.robot.Direction.N;
import static com.robot.Direction.S;
import static com.robot.Direction.W;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

class SimulationTest {
    private static final Instruction FORWARD = new MoveForward();
    private static final Instruction LEFT = new TurnLeft();
    private static final Instruction RIGHT = new TurnRight();

    @Test
    void executesMovementAndTurnsInOrder() {
        SimulationInput simulationInput = mission(3, 3, program(1, 1, N, FORWARD, RIGHT, FORWARD, LEFT, FORWARD));

        assertEquals(List.of(alive(2, 3, N)), Simulation.run(simulationInput));
    }

    @ParameterizedTest
    @CsvSource({"1, 2, N", "2, 1, E", "1, 0, S", "0, 1, W"})
    void losesRobotWhenItCrossesAnEdge(int x, int y, Direction direction) {
        SimulationInput simulationInput = mission(2, 2, program(x, y, direction, FORWARD));

        assertEquals(List.of(lost(x, y, direction)), Simulation.run(simulationInput));
    }

    @Test
    void stopsExecutingInstructionsAfterLoss() {
        Instruction mustNotRun = (robot, world) -> fail("Instructions after loss must not execute");
        SimulationInput simulationInput = mission(1, 1, program(1, 1, N, FORWARD, mustNotRun));

        assertEquals(List.of(lost(1, 1, N)), Simulation.run(simulationInput));
    }

    @Test
    void laterRobotIgnoresScentedExitAndContinuesItsProgram() {
        SimulationInput simulationInput = mission(2, 1,
                program(1, 1, N, FORWARD),
                program(1, 1, N, FORWARD, RIGHT, FORWARD));

        assertEquals(List.of(lost(1, 1, N), alive(2, 1, E)), Simulation.run(simulationInput));
    }

    @Test
    void scentDoesNotBlockMovementInsideTheWorld() {
        SimulationInput simulationInput = mission(1, 1,
                program(1, 1, N, FORWARD),
                program(1, 1, W, FORWARD));

        assertEquals(List.of(lost(1, 1, N), alive(0, 1, W)), Simulation.run(simulationInput));
    }

    @Test
    void cornerScentProtectsADifferentOutwardHeading() {
        SimulationInput simulationInput = mission(1, 1,
                program(0, 0, W, FORWARD),
                program(0, 0, S, FORWARD));

        assertEquals(List.of(lost(0, 0, W), alive(0, 0, S)), Simulation.run(simulationInput));
    }

    @Test
    void scentDoesNotProtectAnotherCoordinate() {
        SimulationInput simulationInput = mission(1, 1,
                program(0, 1, N, FORWARD),
                program(1, 1, N, FORWARD));

        assertEquals(List.of(lost(0, 1, N), lost(1, 1, N)), Simulation.run(simulationInput));
    }

    @Test
    void eachRunStartsWithoutScents() {
        SimulationInput simulationInput = mission(1, 1, program(1, 1, N, FORWARD));
        List<RobotState> expected = List.of(lost(1, 1, N));

        assertEquals(expected, Simulation.run(simulationInput));
        assertEquals(expected, Simulation.run(simulationInput));
    }

    @Test
    void emptyProgramLeavesRobotAtItsStartingPosition() {
        SimulationInput simulationInput = mission(2, 2, program(1, 1, E));

        assertEquals(List.of(alive(1, 1, E)), Simulation.run(simulationInput));
    }

    @Test
    void example1() {
        SimulationInput simulationInput = mission(5, 3, program(1, 1, E, RIGHT, LEFT, RIGHT, LEFT, RIGHT, LEFT, RIGHT, LEFT));
        List<RobotState> expected = List.of(alive(1, 1, E));

        assertEquals(expected, Simulation.run(simulationInput));
    }

    @Test
    void example2() {
        SimulationInput simulationInput = mission(5, 3, program(3, 2, N, FORWARD, RIGHT, RIGHT, FORWARD, LEFT, LEFT, FORWARD, FORWARD, RIGHT, RIGHT, FORWARD, LEFT, LEFT));
        List<RobotState> expected = List.of(lost(3, 3, N));

        assertEquals(expected, Simulation.run(simulationInput));
    }

    @Test
    void example3() {
        SimulationInput simulationInput = mission(5, 3, program(0, 3, W, LEFT, LEFT, FORWARD, FORWARD, FORWARD, LEFT, FORWARD, LEFT, FORWARD, LEFT));
        List<RobotState> expected = List.of(lost(3, 3, N));

        assertEquals(expected, Simulation.run(simulationInput));
    }

    @Test
    void example4() {
        SimulationInput simulationInput = mission(5, 3,
                program(1, 1, E, RIGHT, LEFT, RIGHT, LEFT, RIGHT, LEFT, RIGHT, LEFT),
                program(3, 2, N, FORWARD, RIGHT, RIGHT, FORWARD, LEFT, LEFT, FORWARD, FORWARD, RIGHT, RIGHT, FORWARD, LEFT, LEFT),
                program(0, 3, W, LEFT, LEFT, FORWARD, FORWARD, FORWARD, LEFT, FORWARD, LEFT, FORWARD, LEFT));

        List<RobotState> expected = List.of(
                alive(1, 1, E),
                lost(3, 3, N),
                alive(2, 3, S)
        );

        assertEquals(expected, Simulation.run(simulationInput));
    }

    @Test
    void emptyMissionProducesNoResults() {
        assertEquals(List.of(), Simulation.run(mission(1, 1)));
    }

    @Test
    void rejectsNullMission() {
        assertThrows(NullPointerException.class, () -> Simulation.run(null));
    }

    @Test
    void rejectsAnInstructionThatReturnsNull() {
        Instruction invalidInstruction = (robot, world) -> null;
        SimulationInput simulationInput = mission(1, 1, program(0, 0, N, invalidInstruction));

        assertThrows(NullPointerException.class, () -> Simulation.run(simulationInput));
    }

    private static SimulationInput mission(int maxX, int maxY, RobotProgram... programs) {
        return new SimulationInput(new GridBounds(maxX, maxY), List.of(programs));
    }

    private static RobotProgram program(int x, int y, Direction direction, Instruction... instructions) {
        return new RobotProgram(position(x, y, direction), List.of(instructions));
    }

    private static RobotState alive(int x, int y, Direction direction) {
        return new RobotState(position(x, y, direction), false);
    }

    private static RobotState lost(int x, int y, Direction direction) {
        return new RobotState(position(x, y, direction), true);
    }

    private static Position position(int x, int y, Direction direction) {
        return new Position(new Coordinate(x, y), direction);
    }
}
