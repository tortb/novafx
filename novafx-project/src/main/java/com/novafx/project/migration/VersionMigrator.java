package com.novafx.project.migration;

import com.novafx.core.error.ErrorCode;
import com.novafx.core.error.ErrorCollector;
import com.novafx.project.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Migrates a {@link Project} forward to the current file format version.
 * <p>
 * Migration is <em>not</em> automatically applied — the caller must check
 * {@link #needsMigration} and call {@link #migrate} explicitly, or use
 * the convenience method {@link #ensureCurrent} which does both.
 * <p>
 * Each file format version maps to a {@link MigrationStep}.  Steps are
 * applied in version‑string order until the project reaches
 * {@value Project#CURRENT_VERSION}.
 */
public final class VersionMigrator {

    private static final Logger log = LoggerFactory.getLogger(VersionMigrator.class);

    private final List<MigrationStep> steps;

    /** Creates a migrator with all registered migration steps. */
    public VersionMigrator() {
        this.steps = new ArrayList<>();
        registerDefaults();
    }

    /** Creates a migrator with a custom step list (useful for testing). */
    VersionMigrator(List<MigrationStep> steps) {
        this.steps = new ArrayList<>(steps);
        this.steps.sort(Comparator.comparing(MigrationStep::sourceVersion));
    }

    private void registerDefaults() {
        // No migrations yet — 1.0 is the current version.
        // Add steps here when 1.1 ships, e.g.:
        //   steps.add(new V1ToV1_1Step());
    }

    // ── Queries ──

    /**
     * Returns {@code true} when the project's version is older than the
     * current file format version.
     */
    public boolean needsMigration(Project project) {
        return !Project.CURRENT_VERSION.equals(project.version());
    }

    // ── Migration ──

    /**
     * Migrates the project to the current version if needed.
     * <p>
     * When the version is already current the project is returned unchanged.
     * Warnings are emitted for unrecognised version strings but migration
     * continues optimistically.
     *
     * @param project   the project to migrate
     * @param collector collector for warnings / errors
     * @return the migrated project (or the original if already current)
     */
    public Project ensureCurrent(Project project, ErrorCollector collector) {
        if (!needsMigration(project)) {
            return project;
        }

        String current = project.version();
        log.info("Migrating project from version '{}' to '{}'", current, Project.CURRENT_VERSION);

        Project migrated = project;
        boolean applied = false;

        for (MigrationStep step : steps) {
            if (step.sourceVersion().equals(migrated.version())) {
                try {
                    migrated = step.migrate(migrated);
                    applied = true;
                    log.debug("Applied migration step {} -> ?", step.sourceVersion());
                } catch (Exception e) {
                    collector.addError(ErrorCode.MIGRATION_FAILED,
                            "Migration from version '" + step.sourceVersion()
                                    + "' failed: " + e.getMessage());
                    return project; // return original on failure
                }
            }
        }

        if (!applied) {
            collector.addWarning(ErrorCode.UNKNOWN_VERSION,
                    "Unknown version '" + current + "' — proceeding without migration; "
                            + "the project may not load correctly");
            return project;
        }

        // Final sanity check — the migrator should set the version
        if (!Project.CURRENT_VERSION.equals(migrated.version())) {
            log.warn("After migration chain version is still '{}', not '{}'",
                    migrated.version(), Project.CURRENT_VERSION);
        }

        return migrated;
    }

    /**
     * Returns the number of registered migration steps.
     */
    public int stepCount() {
        return steps.size();
    }
}
