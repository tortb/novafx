package com.novafx.export;

import com.novafx.core.domain.Project;
import com.novafx.math.AstFunctionSampler;
import com.novafx.math.Vector3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Exports a project's sampled points as a CSV file.
 * <p>
 * Format: {@code x,y,z} with one point per line and a header row.
 */
public final class CsvExporter implements Exporter {

    private static final Logger log = LoggerFactory.getLogger(CsvExporter.class);

    private final AstFunctionSampler sampler;

    public CsvExporter() {
        this.sampler = new AstFunctionSampler();
    }

    @Override
    public void export(Project project, Path output) {
        Objects.requireNonNull(project, "project must not be null");
        Objects.requireNonNull(output, "output must not be null");

        List<Vector3d> points = sampler.sample(project.functionDefinition());

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("x,y,z\n");
            for (Vector3d p : points) {
                sb.append(p.x()).append(',')
                        .append(p.y()).append(',')
                        .append(p.z()).append('\n');
            }
            Files.writeString(output, sb.toString());
            log.info("Exported {} points to CSV: {}", points.size(), output);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write CSV to " + output, e);
        }
    }
}
