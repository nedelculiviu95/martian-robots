package com.robot;

import java.util.Objects;

/**
 * Records a robot's position and whether it is lost so its progress and final result can be tracked
 */

public record RobotState(Position position, boolean isLost) {

    public RobotState {
        Objects.requireNonNull(position, "position");
    }

    public RobotState turnLeft() {
        return withDirection(position.direction().left());
    }

    public RobotState turnRight() {
        return withDirection(position.direction().right());
    }

    private RobotState withDirection(Direction direction) {
        return isLost ? this : new RobotState(new Position(position.coordinate(), direction), false);
    }
}
