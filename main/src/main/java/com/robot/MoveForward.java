package com.robot;

public final class MoveForward implements Instruction {
    public static final char SYMBOL = 'F';

    @Override
    public RobotState execute(RobotState currentRobotState, World world) {
        return world.moveRobot(currentRobotState, currentRobotState.position().direction());
    }

}
