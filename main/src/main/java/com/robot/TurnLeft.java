package com.robot;

public final class TurnLeft implements Instruction {
    public static final char SYMBOL = 'L';

    @Override
    public RobotState execute(RobotState robotState, World world) {
        return robotState.turnLeft();
    }
}
