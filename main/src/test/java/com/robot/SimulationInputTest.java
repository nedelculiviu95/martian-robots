package com.robot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationInputTest {
    private final GridBounds bounds = new GridBounds(2, 2);

    @Test
    void acceptsProgramsAtTheInclusiveWorldCorners() {
        List<RobotProgram> programs = List.of(program(0, 0), program(2, 2));
        SimulationInput input = new SimulationInput(bounds, programs);

        assertEquals(bounds, input.gridBounds());
        assertEquals(programs, input.robotPrograms());
    }

    @Test
    void acceptsAnEmptyListOfPrograms() {
        assertTrue(new SimulationInput(bounds, List.of()).robotPrograms().isEmpty());
    }

    @Test
    void programsCannotChangeAfterValidation() {
        RobotProgram validProgram = program(0, 0);
        List<RobotProgram> suppliedPrograms = new ArrayList<>(List.of(validProgram));
        SimulationInput input = new SimulationInput(bounds, suppliedPrograms);
        suppliedPrograms.add(program(3, 0));

        assertEquals(List.of(validProgram), input.robotPrograms());
        assertThrows(UnsupportedOperationException.class, () -> input.robotPrograms().clear());
    }

    @ParameterizedTest
    @CsvSource({"-1, 0", "3, 0", "0, -1", "0, 3"})
    void rejectsAStartingCoordinateOutsideTheWorld(int x, int y) {
        assertThrows(IllegalArgumentException.class,
                () -> new SimulationInput(bounds, List.of(program(x, y))));
    }

    @Test
    void validatesLaterProgramsAsWellAsTheFirst() {
        assertThrows(IllegalArgumentException.class,
                () -> new SimulationInput(bounds, List.of(program(0, 0), program(3, 0))));
    }

    @Test
    void rejectsMissingBounds() {
        assertThrows(NullPointerException.class, () -> new SimulationInput(null, List.of()));
    }

    @Test
    void rejectsMissingPrograms() {
        assertThrows(NullPointerException.class, () -> new SimulationInput(bounds, null));
    }

    @Test
    void rejectsNullProgramsInsideTheInput() {
        List<RobotProgram> programs = Collections.singletonList(null);

        assertThrows(NullPointerException.class, () -> new SimulationInput(bounds, programs));
    }

    private static RobotProgram program(int x, int y) {
        return new RobotProgram(new Position(new Coordinate(x, y), Direction.N), List.of());
    }
}
