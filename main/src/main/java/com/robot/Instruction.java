package com.robot;

/**
 * Represents one robot command, giving every command a common way to update the robot.
 */

@FunctionalInterface
public interface Instruction {
    RobotState execute(RobotState current, World world);
}
