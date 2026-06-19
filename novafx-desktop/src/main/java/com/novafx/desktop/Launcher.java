package com.novafx.desktop;

/**
 * Bootstrap launcher for NovaFX Studio.
 * <p>
 * JavaFX {@code Application.launch()} requires a class that extends
 * {@link javafx.application.Application}. This class serves as the
 * JVM entry point ({@code main}) method.
 */
public final class Launcher {

    private Launcher() {
        // utility class
    }

    /**
     * Application entry point.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        NovaFXApplication.launch(NovaFXApplication.class, args);
    }
}
