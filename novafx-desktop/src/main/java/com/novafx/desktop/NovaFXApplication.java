package com.novafx.desktop;

import com.novafx.ui.view.MainWindow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JavaFX Application entry point for NovaFX Studio.
 * <p>
 * Initializes the main window and configures application-wide settings
 * such as the uncaught exception handler.
 */
public final class NovaFXApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(NovaFXApplication.class);

    @Override
    public void start(Stage primaryStage) {
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            log.error("Uncaught exception in thread {}", thread.getName(), throwable);
        });

        log.info("Starting NovaFX Studio v1.0");
        new MainWindow(primaryStage);
    }

    @Override
    public void stop() {
        log.info("NovaFX Studio shutting down");
        Platform.exit();
    }
}
