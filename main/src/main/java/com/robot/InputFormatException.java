package com.robot;

/**
 * Describes a problem in the input and its line number so the user knows what to fix
 */

public final class InputFormatException extends Exception {
    private final int line;

    public InputFormatException(int line, String message) {
        super(message);
        this.line = line;
    }

    public int line() {
        return line;
    }
}
