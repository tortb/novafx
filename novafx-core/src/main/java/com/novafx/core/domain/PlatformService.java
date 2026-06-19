package com.novafx.core.domain;

import java.nio.file.Path;

/**
 * Provides platform-specific directory paths following OS conventions.
 * <p>
 * On Linux this follows the XDG Base Directory Specification.
 * On Windows it uses {@code %APPDATA%}.
 * On macOS it uses {@code ~/Library/Application Support}.
 * <p>
 * All NovaFX path resolution must go through this interface;
 * hard-coded paths are forbidden.
 */
public interface PlatformService {

    /**
     * Returns the workspace root directory for NovaFX projects.
     * Default: {@code $XDG_DATA_HOME/novafx/workspace/projects}
     * (or the platform equivalent).
     */
    Path workspaceDirectory();

    /**
     * Returns the configuration directory.
     * Default: {@code $XDG_CONFIG_HOME/novafx}
     * (or the platform equivalent).
     */
    Path configDirectory();

    /**
     * Returns the data directory for application state.
     * Default: {@code $XDG_DATA_HOME/novafx}
     * (or the platform equivalent).
     */
    Path dataDirectory();
}
