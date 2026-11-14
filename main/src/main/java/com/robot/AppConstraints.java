package com.robot;

/**
 * Keeps limits on coordinates, robot commands and input size in one place.
 * Rebuild the application after changing these values.
 */

public final class AppConstraints {
    public static final int MIN_COORDINATE = 0;
    public static final int MAX_COORDINATE = 50;
    public static final int MAX_INSTRUCTIONS = 99;
    public static final int MAX_INPUT_BYTES = 1024 * 1024;

    private AppConstraints() {
    }
}
