package com.novafx.export;

import com.novafx.core.domain.Project;

import java.nio.file.Path;

/**
 * Exports a {@link Project} to a specific output format.
 * <p>
 * Implementations write the project's sampled point data to the
 * specified output path. The output format is determined by the
 * concrete implementation class.
 */
@FunctionalInterface
public interface Exporter {

    /**
     * Exports the project to the given output path.
     * <p>
     * The exporter first samples the project's function definition,
     * then writes the resulting points.
     *
     * @param project the project to export; must not be null
     * @param output  the output file path; must not be null
     * @throws IllegalArgumentException if any argument is null
     * @throws RuntimeException         if an I/O error occurs
     */
    void export(Project project, Path output);
}
