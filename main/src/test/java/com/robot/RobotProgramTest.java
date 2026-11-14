package com.robot;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RobotProgramTest {
    private final Position start = new Position(new Coordinate(1, 1), Direction.N);
    private final Instruction forward = new MoveForward();

    @Test
    void acceptsAnEmptyProgram() {
        RobotProgram program = new RobotProgram(start, List.of());

        assertEquals(start, program.initialPosition());
        assertTrue(program.instructions().isEmpty());
    }

    @Test
    void acceptsTheMaximumInstructionCount() {
        RobotProgram program = new RobotProgram(start, Collections.nCopies(AppConstraints.MAX_INSTRUCTIONS, forward));

        assertEquals(AppConstraints.MAX_INSTRUCTIONS, program.instructions().size());
    }

    @Test
    void rejectsTooManyInstructions() {
        List<Instruction> instructions = Collections.nCopies(AppConstraints.MAX_INSTRUCTIONS + 1, forward);

        assertThrows(IllegalArgumentException.class, () -> new RobotProgram(start, instructions));
    }

    @Test
    void instructionsCannotChangeAfterConstruction() {
        List<Instruction> suppliedInstructions = new ArrayList<>(List.of(forward));
        RobotProgram program = new RobotProgram(start, suppliedInstructions);
        suppliedInstructions.clear();

        assertEquals(List.of(forward), program.instructions());
        assertThrows(UnsupportedOperationException.class, () -> program.instructions().clear());
    }

    @Test
    void rejectsMissingPosition() {
        assertThrows(NullPointerException.class, () -> new RobotProgram(null, List.of()));
    }

    @Test
    void rejectsMissingInstructions() {
        assertThrows(NullPointerException.class, () -> new RobotProgram(start, null));
    }

    @Test
    void rejectsNullInstructionsInsideAProgram() {
        List<Instruction> instructions = Collections.singletonList(null);

        assertThrows(NullPointerException.class, () -> new RobotProgram(start, instructions));
    }
}
