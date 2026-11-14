package com.robot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Runs each robot's commands in order and collects the results, letting later robots use earlier warnings
 */

public final class Simulation {

    private Simulation() {}

    public static List<RobotState> run(SimulationInput simulationInput) {
        Objects.requireNonNull(simulationInput, "SimulationInput");

        World world = new World(simulationInput.gridBounds());
        List<RobotState> results = new ArrayList<>(simulationInput.robotPrograms().size());

        for (RobotProgram program : simulationInput.robotPrograms()) {
            RobotState state = new RobotState(program.initialPosition(), false);

            for (Instruction instruction : program.instructions()) {
                state = Objects.requireNonNull(instruction.execute(state, world), "instruction result");

                if (state.isLost()) {
                    break;
                }
            }

            results.add(state);
        }

        return results;
    }

}
