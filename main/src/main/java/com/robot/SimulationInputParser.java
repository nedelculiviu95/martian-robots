package com.robot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns the user's text into grid details and robot programs, reporting mistakes before any robots move
 */

public final class SimulationInputParser {
    private static final Pattern INPUT_FIELD = Pattern.compile("[^ \\t]+"); // Any character except a space or tab (one or more)
    private static final Pattern COORDINATE = Pattern.compile("0|[1-9][0-9]*"); // Zero or a positive integer without leading zeros.
    private static final int GRID_BOUNDS_LINE_NUMBER = 1;
    private static final int LINES_PER_ROBOT = 2;

    private final Map<Character, Instruction> robotInstructions;

    public SimulationInputParser(Map<Character, Instruction> robotInstructions) {
        this.robotInstructions = Map.copyOf(robotInstructions);
    }

    public SimulationInput parse(Reader input) throws IOException, InputFormatException {
        Objects.requireNonNull(input, "input");

        BufferedReader reader = new BufferedReader(input);
        GridBounds gridBounds = readUpperRightGridBounds(reader);
        List<RobotProgram> robotPrograms = readRobotInitialPositionAndInstructions(reader, gridBounds);

        return new SimulationInput(gridBounds, robotPrograms);
    }

    /**
     * Read first line of the input which should contain 2 coordinates only (e.g. 5 3)
     */
    private GridBounds readUpperRightGridBounds(BufferedReader reader) throws IOException, InputFormatException {
        String header = reader.readLine();
        if (header == null) {
            throw new InputFormatException(GRID_BOUNDS_LINE_NUMBER, "Missing world gridBounds");
        }

        List<String> worldUpperBoundFields = fields(header, GRID_BOUNDS_LINE_NUMBER, 2, "World upper-right grid bounds require exactly two coordinates");
        return new GridBounds(
                parseCoordinate(worldUpperBoundFields.get(0), GRID_BOUNDS_LINE_NUMBER),
                parseCoordinate(worldUpperBoundFields.get(1), GRID_BOUNDS_LINE_NUMBER)
        );
    }

    /**
     * Read robot initial positon (e.g. 1 1 E) and the movement instructions (e.g. RFRFRFRF)
     */
    private List<RobotProgram> readRobotInitialPositionAndInstructions(BufferedReader reader, GridBounds gridBounds)
            throws IOException, InputFormatException {
        List<RobotProgram> robots = new ArrayList<>();
        int positionLineNumber = 2;
        String positionLine;

        while ((positionLine = reader.readLine()) != null) {
            Position position = parseInitialPosition(positionLine, positionLineNumber, gridBounds);
            int instructionLineNumber = positionLineNumber + 1;

            String instructionLine = reader.readLine();
            if (instructionLine == null) {
                throw new InputFormatException(instructionLineNumber, "Missing instruction line");
            }

            List<Instruction> instructions = parseRobotInstructions(instructionLine, instructionLineNumber);
            robots.add(new RobotProgram(position, instructions));
            positionLineNumber += LINES_PER_ROBOT;
        }

        return robots;
    }

    /**
     * Parse robot initial positon (e.g. 1 1 E) to Position class (coordinate=(1,1) and direction=E)
     */
    private Position parseInitialPosition(String line, int lineNumber, GridBounds gridBounds) throws InputFormatException {
        List<String> positionFields = fields(line, lineNumber, 3, "Robot position requires exactly two coordinates and an orientation");
        Coordinate coordinate = new Coordinate(parseCoordinate(positionFields.get(0), lineNumber), parseCoordinate(positionFields.get(1), lineNumber));
        Direction direction = parseDirection(positionFields.get(2), lineNumber);

        if (!gridBounds.contains(coordinate)) {
            throw new InputFormatException(lineNumber,
                    "Initial position is outside world gridBounds " + AppConstraints.MIN_COORDINATE + ".."
                            + gridBounds.maxX() + ", " + AppConstraints.MIN_COORDINATE + ".." + gridBounds.maxY());
        }

        return new Position(coordinate, direction);
    }

    private List<Instruction> parseRobotInstructions(String line, int lineNumber) throws InputFormatException {
        if (line.length() > AppConstraints.MAX_INSTRUCTIONS) {
            throw new InputFormatException(lineNumber, "Instruction program exceeds " + AppConstraints.MAX_INSTRUCTIONS + " characters");
        }

        List<Instruction> program = new ArrayList<>(line.length());
        for (int i = 0; i < line.length(); i++) {
            char symbol = line.charAt(i);
            Instruction instruction = robotInstructions.get(symbol);

            if (instruction == null || Character.isWhitespace(symbol)) {
                String message = Character.isWhitespace(symbol)
                        ? "whitespace is not allowed in an instruction program."
                        : "Unknown instruction: " + symbol;
                throw new InputFormatException(lineNumber, message);
            }

            program.add(instruction);
        }

        return program;
    }

    private List<String> fields(String line, int lineNumber, int expected, String message)
            throws InputFormatException {
        List<String> fields = new ArrayList<>();
        Matcher matcher = INPUT_FIELD.matcher(line);

        while (matcher.find()) {
            fields.add(matcher.group());
        }

        if (fields.size() != expected) {
            throw new InputFormatException(lineNumber, message);
        }

        return fields;
    }

    private int parseCoordinate(String field, int lineNumber) throws InputFormatException {
        int value;

        try {
            if (!COORDINATE.matcher(field).matches()) {
                throw new NumberFormatException();
            }

            value = Integer.parseInt(field);
        } catch (NumberFormatException exception) {
            throw new InputFormatException(lineNumber,
                    "Coordinate must be a decimal integer between " + AppConstraints.MIN_COORDINATE + " and " + AppConstraints.MAX_COORDINATE
                            + ", without leading zeros");
        }

        if (!GridBounds.isValidCoordinate(value)) {
            throw new InputFormatException(lineNumber,
                    "Coordinate must be between " + AppConstraints.MIN_COORDINATE + " and " + AppConstraints.MAX_COORDINATE);
        }

        return value;
    }

    private Direction parseDirection(String field, int lineNumber) throws InputFormatException {
        try {
            return Direction.valueOf(field);
        } catch (IllegalArgumentException exception) {
            throw new InputFormatException(lineNumber, "Orientation must be N, E, S or W");
        }
    }
}
