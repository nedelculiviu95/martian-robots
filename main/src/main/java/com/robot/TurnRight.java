package com.robot;

public final class TurnRight implements Instruction {
    public static final char SYMBOL = 'R';

    @Override
    public RobotState execute(RobotState robotState, World world) {
        return robotState.turnRight();
    }
}
