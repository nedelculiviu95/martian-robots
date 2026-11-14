package com.robot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationInputParserTest {
    private final Instruction LEFT = new TurnLeft();
    private final Instruction RIGHT = new TurnRight();
    private final Instruction FORWARD = new MoveForward();
    private final SimulationInputParser parser = new SimulationInputParser(Map.of(
            TurnLeft.SYMBOL, LEFT, TurnRight.SYMBOL, RIGHT, MoveForward.SYMBOL, FORWARD));

    @Test
    void parsesWorldBoundsAndRobotProgram() throws Exception {
        SimulationInput simulationInput = parse("5 3\n1 1 E\nF\n");
        RobotProgram robot = simulationInput.robotPrograms().getFirst();

        assertEquals(new GridBounds(5, 3), simulationInput.gridBounds());
        assertEquals(1, simulationInput.robotPrograms().size());

        assertEquals(new Position(new Coordinate(1, 1), Direction.E), robot.initialPosition());
        assertEquals(List.of(FORWARD), robot.instructions());
    }

    @Test
    void acceptsLargestParseCoordinate() throws Exception {
        int maximum = AppConstraints.MAX_COORDINATE;
        SimulationInput simulationInput = parse(maximum + " " + maximum + "\n" + maximum + " " + maximum + " N\n\n");

        assertEquals(new GridBounds(maximum, maximum), simulationInput.gridBounds());
        assertEquals(new Coordinate(maximum, maximum), simulationInput.robotPrograms().getFirst().initialPosition().coordinate());
    }

    @Test
    void preservesAnEmptyProgramBeforeTheNextRobot() throws Exception {
        SimulationInput simulationInput = parse("5 3\n0 0 N\n\n1 1 E\nF\n");

        assertEquals(2, simulationInput.robotPrograms().size());
        assertTrue(simulationInput.robotPrograms().getFirst().instructions().isEmpty());
        assertEquals(new Position(new Coordinate(1, 1), Direction.E), simulationInput.robotPrograms().get(1).initialPosition());
        assertEquals(List.of(FORWARD), simulationInput.robotPrograms().get(1).instructions());
    }

    @Test
    void rejectsParseCoordinateAboveTheLimit() {
        String input = (AppConstraints.MAX_COORDINATE + 1) + " 3\n";

        InputFormatException error = assertThrows(InputFormatException.class, () -> parse(input));

        assertEquals(1, error.line());
    }

    @Test
    void reportsTheLineOfAnInvalidOrientation() {
        InputFormatException error = assertThrows(InputFormatException.class, () -> parse("5 3\n1 1 X\nF\n"));

        assertEquals(2, error.line());
        assertTrue(error.getMessage().startsWith("Orientation must be N, E, S or W"));
    }

    @Test
    void reportsTheLineOfAnInvalidInstructionInALaterRobot() {
        InputFormatException error = assertThrows(InputFormatException.class, () -> parse("5 3\n0 0 N\nF\n1 1 E\nX\n"));

        assertEquals(5, error.line());
        assertTrue(error.getMessage().startsWith("Unknown instruction: X"));
    }

    @Test
    void rejectsMissingInstructionLine() {
        InputFormatException error = assertThrows(InputFormatException.class, () -> parse("5 3\n1 1 N\n"));

        assertEquals(3, error.line());
        assertTrue(error.getMessage().startsWith("Missing instruction line"));
    }

    @Test
    void rejectsMissingWorldBounds() {
        InputFormatException error = assertThrows(InputFormatException.class, () -> parse(""));

        assertEquals(1, error.line());
        assertTrue(error.getMessage().startsWith("Missing world"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " \t", "5", "5 3 1"})
    void rejectsMalformedWorldBounds(String initialPosition) {
        InputFormatException error = assertThrows(InputFormatException.class, () -> parse(initialPosition + "\n"));

        assertEquals(1, error.line());
        assertTrue(error.getMessage().contains("exactly two coordinates"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " \t", "1 1", "1 1 N extra"})
    void rejectsMalformedRobotPositions(String position) {
        InputFormatException error = assertThrows(InputFormatException.class, () -> parse("5 3\n" + position + "\nF\n"));

        assertEquals(2, error.line());
        assertTrue(error.getMessage().contains("Robot position requires"));
    }

    @ParameterizedTest
    @CsvSource({"-1, 0", "0, -1", "abc, 0", "0, abc", "1.5, 0", "0, 2147483648"})
    void rejectsInvalidWorldCoordinates(String x, String y) {
        InputFormatException error = assertThrows(InputFormatException.class, () -> parse(x + " " + y + "\n"));

        assertEquals(1, error.line());
        assertTrue(error.getMessage().startsWith("Coordinate must be"));
    }

    @ParameterizedTest
    @CsvSource({"-1, 0", "0, abc", "2147483648, 0"})
    void rejectsInvalidRobotCoordinates(String x, String y) {
        InputFormatException error = assertThrows(InputFormatException.class, () -> parse("5 3\n" + x + " " + y + " N\nF\n"));

        assertEquals(2, error.line());
        assertTrue(error.getMessage().startsWith("Coordinate must be"));
    }

    @ParameterizedTest
    @CsvSource({"6, 0", "0, 4"})
    void rejectsRobotPositionsOutsideTheActualWorld(int x, int y) {
        InputFormatException error = assertThrows(InputFormatException.class, () -> parse("5 3\n" + x + " " + y + " N\nF\n"));

        assertEquals(2, error.line());
        assertTrue(error.getMessage().startsWith("Initial position is outside world"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"n", "NE"})
    void rejectsLowercaseAndMultiLetterOrientations(String orientation) {
        InputFormatException error = assertThrows(InputFormatException.class,
                () -> parse("5 3\n1 1 " + orientation + "\nF\n"));

        assertEquals(2, error.line());
        assertTrue(error.getMessage().startsWith("Orientation must be"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"N", "E", "S", "W"})
    void acceptsEachCardinalOrientation(Direction direction) throws Exception {
        SimulationInput simulationInput = parse("5 3\n1 1 " + direction + "\nF\n");

        assertEquals(direction, simulationInput.robotPrograms().getFirst().initialPosition().direction());
    }

    @ParameterizedTest
    @CsvSource({"0, 0", "0, 3", "3, 0"})
    void acceptsZeroAndSingleAxisWorlds(int maxX, int maxY) throws Exception {
        SimulationInput simulationInput = parse(maxX + " " + maxY + "\n0 0 N\n\n");

        assertEquals(new GridBounds(maxX, maxY), simulationInput.gridBounds());
        assertTrue(simulationInput.robotPrograms().getFirst().instructions().isEmpty());
    }

    @Test
    void acceptsHeaderWithoutRobots() throws Exception {
        assertTrue(parse("5 3\n").robotPrograms().isEmpty());
    }

    @Test
    void acceptsTabsRepeatedSpacesAndCrLf() throws Exception {
        SimulationInput simulationInput = parse(" \t5  3\t\r\n\t1  2\tE \r\nF\r\n");

        assertEquals(new GridBounds(5, 3), simulationInput.gridBounds());
        assertEquals(new Position(new Coordinate(1, 2), Direction.E), simulationInput.robotPrograms().getFirst().initialPosition());
    }

    @ParameterizedTest
    @CsvSource({"00, 3", "01, 3", "005, 3", "5, 00", "5, 03", "5, 003"})
    void rejectsLeadingZerosInWorldCoordinates(String x, String y) {
        InputFormatException error = assertThrows(InputFormatException.class, () -> parse(x + " " + y + "\n"));

        assertEquals(1, error.line());
        assertTrue(error.getMessage().contains("without leading zeros"));
    }

    @ParameterizedTest
    @CsvSource({"00, 1", "01, 1", "001, 1", "1, 00", "1, 01", "1, 001"})
    void rejectsLeadingZerosInRobotCoordinates(String x, String y) {
        InputFormatException error = assertThrows(InputFormatException.class,
                () -> parse("5 3\n" + x + " " + y + " N\nF\n"));

        assertEquals(2, error.line());
        assertTrue(error.getMessage().contains("without leading zeros"));
    }

    @Test
    void acceptsCompleteProgramWithoutFinalNewline() throws Exception {
        assertEquals(List.of(FORWARD), parse("5 3\n1 1 N\nF").robotPrograms().getFirst().instructions());
    }

    @Test
    void preservesInstructionOrder() throws Exception {
        SimulationInput simulationInput = parse("5 3\n1 1 N\nRLF\n");

        assertEquals(List.of(RIGHT, LEFT, FORWARD), simulationInput.robotPrograms().getFirst().instructions());
    }

    @Test
    void acceptsMaximumInstructionLength() throws Exception {
        String instructions = "F".repeat(AppConstraints.MAX_INSTRUCTIONS);

        assertEquals(AppConstraints.MAX_INSTRUCTIONS,
                parse("5 3\n1 1 N\n" + instructions + "\n").robotPrograms().getFirst().instructions().size());
    }

    @Test
    void rejectsOneInstructionBeyondTheLimit() {
        String instructions = "F".repeat(AppConstraints.MAX_INSTRUCTIONS + 1);
        InputFormatException error = assertThrows(InputFormatException.class,
                () -> parse("5 3\n1 1 N\n" + instructions + "\n"));

        assertEquals(3, error.line());
        assertTrue(error.getMessage().contains("exceeds " + AppConstraints.MAX_INSTRUCTIONS));
    }

    @ParameterizedTest
    @ValueSource(strings = {"f", "B", " F", "F ", "F F", "F\tF"})
    void rejectsUnknownOrWhitespaceInstructions(String instructions) {
        InputFormatException error = assertThrows(InputFormatException.class, () -> parse("5 3\n1 1 N\n" + instructions + "\n"));

        assertEquals(3, error.line());
    }

    @Test
    void validatesInstructionsAfterAMoveThatWouldLoseTheRobot() {
        InputFormatException error = assertThrows(InputFormatException.class,
                () -> parse("0 0\n0 0 N\nFX\n"));

        assertEquals(3, error.line());
        assertTrue(error.getMessage().contains("X"));
    }

    @Test
    void rejectsLongParseCoordinate() {
        InputFormatException error = assertThrows(InputFormatException.class, () -> parse("2147483648 1\n0 0 N\nF\n1 1 E\n"));

        assertEquals(1, error.line());
        assertTrue(error.getMessage().startsWith("Coordinate must be a decimal integer between"));
    }

    @Test
    void rejectsMissingInstructionLineForALaterRobot() {
        InputFormatException error = assertThrows(InputFormatException.class,
                () -> parse("5 3\n0 0 N\nF\n1 1 E\n"));

        assertEquals(5, error.line());
        assertTrue(error.getMessage().startsWith("Missing instruction line"));
    }

    @Test
    void rejectsTrailingBlankPositionLine() {
        InputFormatException error = assertThrows(InputFormatException.class,
                () -> parse("5 3\n0 0 N\nF\n\n"));

        assertEquals(4, error.line());
        assertTrue(error.getMessage().contains("Robot position requires"));
    }

    @Test
    void acceptsAnInstructionAddedToTheRegistry() throws Exception {
        Instruction backward = (robot, world) -> world.moveRobot(robot, robot.position().direction().opposite());
        SimulationInputParser extendedParser = new SimulationInputParser(Map.of('B', backward));
        SimulationInput simulationInput = extendedParser.parse(new StringReader("5 3\n1 1 N\nB\n"));

        assertEquals(List.of(backward), simulationInput.robotPrograms().getFirst().instructions());
    }

    @Test
    void keepsItsOwnCopyOfTheInstructionRegistry() throws Exception {
        Map<Character, Instruction> registry = new HashMap<>(Map.of(MoveForward.SYMBOL, FORWARD));
        SimulationInputParser isolatedParser = new SimulationInputParser(registry);
        registry.clear();

        SimulationInput simulationInput = isolatedParser.parse(new StringReader("5 3\n1 1 N\nF\n"));
        assertEquals(List.of(FORWARD), simulationInput.robotPrograms().getFirst().instructions());
    }

    @Test
    void rejectsWhitespaceEvenWhenItIsRegistered() {
        SimulationInputParser whitespaceParser = new SimulationInputParser(Map.of(' ', FORWARD));
        InputFormatException error = assertThrows(InputFormatException.class,
                () -> whitespaceParser.parse(new StringReader("5 3\n1 1 N\n \n")));

        assertEquals(3, error.line());
        assertTrue(error.getMessage().contains("whitespace is not allowed"));
    }

    @Test
    void rejectsNullInput() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    void propagatesReaderFailures() {
        IOException failure = new IOException("read failed");
        StringReader input = new StringReader("") {
            @Override
            public int read(char[] buffer, int offset, int length) throws IOException {
                throw failure;
            }
        };

        assertSame(failure, assertThrows(IOException.class, () -> parser.parse(input)));
    }

    private SimulationInput parse(String input) throws IOException, InputFormatException {
        return parser.parse(new StringReader(input));
    }
}
