package com.novafx.export;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
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
 * Exports a project's sampled points as a JSON file.
 * <p>
 * Format: {@code {"points": [{"x": ..., "y": ..., "z": ...}, ...]}}
 */
public final class JsonExporter implements Exporter {

    private static final Logger log = LoggerFactory.getLogger(JsonExporter.class);

    private final AstFunctionSampler sampler;
    private final JsonMapper mapper;

    public JsonExporter() {
        this.sampler = new AstFunctionSampler();
        this.mapper = (JsonMapper) JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();
    }

    @Override
    public void export(Project project, Path output) {
        Objects.requireNonNull(project, "project must not be null");
        Objects.requireNonNull(output, "output must not be null");

        List<Vector3d> points = sampler.sample(project.functionDefinition());

        var jsonPoints = points.stream()
                .map(p -> new PointJson(p.x(), p.y(), p.z()))
                .toList();

        try {
            mapper.writeValue(output.toFile(), java.util.Map.of("points", jsonPoints));
            log.info("Exported {} points to JSON: {}", points.size(), output);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write JSON to " + output, e);
        }
    }

    private record PointJson(double x, double y, double z) {
    }
}
