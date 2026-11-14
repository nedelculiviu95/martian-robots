package com.robot;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents the grid and remembers where robots were lost (specific coordinate) so later robots can avoid falling off there
 */

public final class World {
    private final GridBounds gridBounds;
    private final Set<Coordinate> scents = new HashSet<>();

    public World(GridBounds gridBounds) {
        this.gridBounds = Objects.requireNonNull(gridBounds, "gridBounds");
    }

    public RobotState moveRobot(RobotState currentRobotState, Direction direction) {
        Objects.requireNonNull(currentRobotState, "currentRobotState");
        Objects.requireNonNull(direction, "direction");

        if (currentRobotState.isLost()) {
            return currentRobotState;
        }

        Coordinate coordinate = currentRobotState.position().coordinate();
        if (!gridBounds.contains(coordinate)) {
            throw new IllegalArgumentException("Robot must start its movement inside the world");
        }

        Coordinate futureRobotPosition = new Coordinate(coordinate.x() + direction.dx(), coordinate.y() + direction.dy());
        if (gridBounds.contains(futureRobotPosition)) {
            return new RobotState(new Position(futureRobotPosition, currentRobotState.position().direction()), false);
        }

        if (scents.contains(coordinate)) {
            return currentRobotState;
        }

        scents.add(coordinate);
        return new RobotState(currentRobotState.position(), true);
    }
}
