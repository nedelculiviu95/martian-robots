package com.robot;

import java.util.List;
import java.util.Objects;

/**
 * Holds one robot's starting position and commands so the simulation knows what it should do
 */

public record RobotProgram(Position initialPosition, List<Instruction> instructions) {

    public RobotProgram {
        Objects.requireNonNull(initialPosition, "initialPosition");

        instructions = List.copyOf(instructions);
        if (instructions.size() > AppConstraints.MAX_INSTRUCTIONS) {
            throw new IllegalArgumentException("Instruction program exceeds " + AppConstraints.MAX_INSTRUCTIONS + " characters");
        }
    }

}
