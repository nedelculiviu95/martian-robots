package com.robot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Starts the application, reads the user's input, runs the simulation and displays results or errors.
 */

public final class Main {
    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_FAILURE = 1;
    private static final int EXIT_INVALID_INPUT = 2;

    private Main() {}

    public static void main(String[] args) {
        int status = run(System.in, System.out, System.err);

        if (status != 0) {
            System.exit(status);
        }
    }

    public static int run(InputStream input, OutputStream output, OutputStream error) {
        try {
            byte[] bytes = input.readNBytes(AppConstraints.MAX_INPUT_BYTES + 1);
            if (bytes.length > AppConstraints.MAX_INPUT_BYTES) {
                writeError(error, "Input error: input exceeds the " + AppConstraints.MAX_INPUT_BYTES + " byte limit");
                return EXIT_INVALID_INPUT;
            }

            String text = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();

            Map<Character, Instruction> instructions = Map.of(
                    TurnLeft.SYMBOL, new TurnLeft(),
                    TurnRight.SYMBOL, new TurnRight(),
                    MoveForward.SYMBOL, new MoveForward()
            );
            SimulationInput simulationInput = new SimulationInputParser(instructions).parse(new StringReader(text));
            List<RobotState> robotsFinalState = Simulation.run(simulationInput);

            String simulationOutput = formatResults(robotsFinalState);
            writeOutput(output, simulationOutput);

            return EXIT_SUCCESS;

        } catch (InputFormatException exception) {
            writeError(error, "Input error at line " + exception.line() + ": " + exception.getMessage());
            return EXIT_INVALID_INPUT;
        } catch (CharacterCodingException exception) {
            writeError(error, "Input error: input must be valid UTF-8");
            return EXIT_INVALID_INPUT;
        } catch (IOException exception) {
            writeError(error, "I/O error: unable to read input or write output");
            return EXIT_FAILURE;
        } catch (RuntimeException exception) {
            writeError(error, "Application error: unexpected execution failure (" + exception.getClass().getSimpleName() + ")");
            return EXIT_FAILURE;
        }
    }

    private static void writeOutput(OutputStream output, String text) throws IOException {
        output.write(text.getBytes(StandardCharsets.UTF_8));
        output.flush();

        if (output instanceof PrintStream stream && stream.checkError()) {
            throw new IOException("Unable to write output");
        }
    }

    private static String formatResults(List<RobotState> robotsFinalState) {
        StringBuilder output = new StringBuilder();

        for (RobotState state : robotsFinalState) {
            Position position = state.position();
            output.append(position.coordinate().x()).append(' ')
                    .append(position.coordinate().y()).append(' ')
                    .append(position.direction());

            if (state.isLost()) {
                output.append(' ').append("LOST");
            }

            output.append('\n');
        }

        return output.toString();
    }

    private static void writeError(OutputStream error, String message) {
        try {
            error.write((message + "\n").getBytes(StandardCharsets.UTF_8));
            error.flush();
        } catch (IOException ignored) {
            // The nonzero exit status still reports failure if stderr is unavailable.
        }
    }
}
