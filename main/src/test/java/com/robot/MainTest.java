package com.robot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class MainTest {
    private static final String ONE_ROBOT = "1 1\n0 0 N\nF\n";

    @Test
    void printsTheCompleteSampleInRobotOrder() {
        Outcome result = run("5 3\n1 1 E\nRFRFRFRF\n3 2 N\nFRRFLLFFRRFLL\n0 3 W\nLLFFFLFLFL\n");

        assertEquals(0, result.status());
        assertEquals("1 1 E\n3 3 N LOST\n2 3 S\n", result.output());
        assertEquals("", result.error());
    }

    @Test
    void printsNothingWhenThereAreNoRobots() {
        Outcome result = run("1 1\n");

        assertEquals(0, result.status());
        assertEquals("", result.output());
        assertEquals("", result.error());
    }

    @Test
    void rejectsEmptyInput() {
        Outcome result = run("");

        assertEquals(2, result.status());
        assertEquals("", result.output());
        assertTrue(result.error().startsWith("Input error at line 1:"));
    }

    @Test
    void invalidLaterRobotProducesNoPartialOutput() {
        Outcome result = run(ONE_ROBOT + "0 0 N\nX\n");

        assertEquals(2, result.status());
        assertEquals("", result.output());
        assertTrue(result.error().contains("Input error at line 5:"));
    }

    @Test
    void writesUnicodeInputDiagnosticsAsUtf8() {
        Outcome result = run("1 1\n0 0 N\nΩ\n");

        assertEquals(2, result.status());
        assertTrue(result.error().contains("Ω"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0xff, 0xc3})
    void rejectsMalformedAndIncompleteUtf8(int invalidByte) {
        Outcome result = run(new ByteArrayInputStream(new byte[]{(byte) invalidByte}));

        assertEquals(2, result.status());
        assertEquals("", result.output());
        assertTrue(result.error().contains("input must be valid UTF-8"));
    }

    @Test
    void acceptsInputExactlyAtTheByteLimit() {
        String header = "0 0" + " ".repeat(AppConstraints.MAX_INPUT_BYTES - 4) + "\n";

        Outcome result = run(header);

        assertEquals(0, result.status());
        assertEquals("", result.output());
        assertEquals("", result.error());
    }

    @Test
    void stopsReadingAnEndlessStreamJustBeyondTheByteLimit() {
        AtomicInteger reads = new AtomicInteger();
        InputStream endlessInput = new InputStream() {
            @Override
            public int read() {
                if (reads.incrementAndGet() > AppConstraints.MAX_INPUT_BYTES + 1) {
                    return fail("Input must not be read beyond the size-limit check");
                }
                return ' ';
            }
        };

        Outcome result = run(endlessInput);

        assertEquals(2, result.status());
        assertEquals(AppConstraints.MAX_INPUT_BYTES + 1, reads.get());
        assertEquals("", result.output());
        assertTrue(result.error().contains("exceeds the " + AppConstraints.MAX_INPUT_BYTES + " byte limit"));
    }

    @Test
    void reportsInputReadFailures() {
        InputStream brokenInput = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("read failed");
            }
        };

        Outcome result = run(brokenInput);

        assertEquals(1, result.status());
        assertEquals("", result.output());
        assertTrue(result.error().startsWith("I/O error:"));
    }

    @Test
    void reportsUnexpectedExecutionFailures() {
        InputStream brokenInput = new InputStream() {
            @Override
            public int read() {
                throw new IllegalStateException("unexpected failure");
            }
        };

        Outcome result = run(brokenInput);

        assertEquals(1, result.status());
        assertTrue(result.error().contains("unexpected execution failure (IllegalStateException)"));
    }

    @Test
    void reportsOutputWriteFailures() {
        ByteArrayOutputStream error = new ByteArrayOutputStream();

        int status = Main.run(input(ONE_ROBOT), failingOutput(), error);

        assertEquals(1, status);
        assertTrue(error.toString(UTF_8).startsWith("I/O error:"));
    }

    @Test
    void reportsOutputFlushFailures() {
        OutputStream output = new ByteArrayOutputStream() {
            @Override
            public void flush() throws IOException {
                throw new IOException("flush failed");
            }
        };
        ByteArrayOutputStream error = new ByteArrayOutputStream();

        assertEquals(1, Main.run(input(ONE_ROBOT), output, error));
        assertTrue(error.toString(UTF_8).startsWith("I/O error:"));
    }

    @Test
    void detectsFailuresSuppressedByPrintStream() {
        PrintStream output = new PrintStream(failingOutput(), false, UTF_8);
        ByteArrayOutputStream error = new ByteArrayOutputStream();

        assertEquals(1, Main.run(input(ONE_ROBOT), output, error));
        assertTrue(output.checkError());
        assertTrue(error.toString(UTF_8).startsWith("I/O error:"));
    }

    @Test
    void preservesFailureStatusWhenErrorOutputCannotBeWritten() {
        int status = Main.run(input(""), new ByteArrayOutputStream(), failingOutput());

        assertEquals(2, status);
    }

    @Test
    void preservesFailureStatusWhenErrorOutputCannotBeFlushed() {
        OutputStream error = new ByteArrayOutputStream() {
            @Override
            public void flush() throws IOException {
                throw new IOException("flush failed");
            }
        };

        assertEquals(2, Main.run(input(""), new ByteArrayOutputStream(), error));
    }

    private static Outcome run(String text) {
        return run(input(text));
    }

    private static Outcome run(InputStream input) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream error = new ByteArrayOutputStream();
        int status = Main.run(input, output, error);
        return new Outcome(status, output.toString(UTF_8), error.toString(UTF_8));
    }

    private static InputStream input(String text) {
        return new ByteArrayInputStream(text.getBytes(UTF_8));
    }

    private static OutputStream failingOutput() {
        return new OutputStream() {
            @Override
            public void write(int value) throws IOException {
                throw new IOException("write failed");
            }
        };
    }

    private record Outcome(int status, String output, String error) {
    }
}
