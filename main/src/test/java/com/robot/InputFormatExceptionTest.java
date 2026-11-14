package com.robot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InputFormatExceptionTest {

    @Test
    void preservesTheLineAndDiagnosticMessage() {
        InputFormatException error = new InputFormatException(5, "Unknown instruction X");

        assertEquals(5, error.line());
        assertEquals("Unknown instruction X", error.getMessage());
    }
}
