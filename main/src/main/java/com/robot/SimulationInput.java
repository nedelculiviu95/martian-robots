package com.robot;

import java.util.List;
import java.util.Objects;

/**
 * Keeps the grid's edges and all robot programs together, checking that every robot starts inside the grid.
 */

public record SimulationInput(GridBounds gridBounds, List<RobotProgram> robotPrograms) {

    public SimulationInput {
        Objects.requireNonNull(gridBounds, "gridBounds");
        Objects.requireNonNull(robotPrograms, "robotPrograms");
        robotPrograms = List.copyOf(robotPrograms); // this is needed so callers can’t modify the list returned by SimulationInput.robotPrograms()

        for (RobotProgram robot : robotPrograms) {
            if (!gridBounds.contains(robot.initialPosition().coordinate())) {
                throw new IllegalArgumentException("Initial robot position is outside the world");
            }
        }
    }
}
