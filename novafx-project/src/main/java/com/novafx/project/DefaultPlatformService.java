package com.novafx.project;

import com.novafx.core.domain.PlatformService;

import java.nio.file.Path;

/**
 * Platform-aware implementation of {@link PlatformService}.
 * <p>
 * Detects the operating system at runtime and returns appropriate
 * directory paths following platform conventions:
 * <ul>
 *   <li><b>Linux:</b> XDG Base Directory Specification</li>
 *   <li><b>Windows:</b> {@code %APPDATA%}</li>
 *   <li><b>macOS:</b> {@code ~/Library/Application Support}</li>
 * </ul>
 */
public final class DefaultPlatformService implements PlatformService {

    private static final String APP_NAME = "novafx";

    @Override
    public Path workspaceDirectory() {
        return dataDirectory().resolve("workspace").resolve("projects");
    }

    @Override
    public Path configDirectory() {
        return baseConfigDir().resolve(APP_NAME);
    }

    @Override
    public Path dataDirectory() {
        return baseDataDir().resolve(APP_NAME);
    }

    // ---------------------------------------------------------------
    // OS detection
    // ---------------------------------------------------------------

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    // ---------------------------------------------------------------
    // Directory resolution
    // ---------------------------------------------------------------

    private static Path baseDataDir() {
        if (isWindows()) {
            return Path.of(System.getenv("APPDATA"));
        }
        if (isMac()) {
            return Path.of(System.getProperty("user.home"), "Library", "Application Support");
        }
        // Linux / Unix: XDG_DATA_HOME or default
        String xdgData = System.getenv("XDG_DATA_HOME");
        if (xdgData != null && !xdgData.isBlank()) {
            return Path.of(xdgData);
        }
        return Path.of(System.getProperty("user.home"), ".local", "share");
    }

    private static Path baseConfigDir() {
        if (isWindows()) {
            return Path.of(System.getenv("APPDATA"));
        }
        if (isMac()) {
            return Path.of(System.getProperty("user.home"), "Library", "Application Support");
        }
        // Linux / Unix: XDG_CONFIG_HOME or default
        String xdgConfig = System.getenv("XDG_CONFIG_HOME");
        if (xdgConfig != null && !xdgConfig.isBlank()) {
            return Path.of(xdgConfig);
        }
        return Path.of(System.getProperty("user.home"), ".config");
    }
}
