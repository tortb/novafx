package com.novafx.project.io;

import com.novafx.project.model.Meta;
import com.novafx.project.model.ParticleSettings;
import com.novafx.project.model.Project;
import com.novafx.project.model.RenderSettings;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes {@code .nfx} files in TOML v1.0 format with UTF-8 encoding
 * and LF line endings.
 * <p>
 * The output is human-readable, Git-friendly, and AI-friendly.
 */
public final class NfxWriter implements ProjectWriter {

    private static final Logger log = LoggerFactory.getLogger(NfxWriter.class);

    @Override
    public void write(Project project, Path path) {
        String toml = serialize(project);
        try {
            Files.writeString(path, toml, StandardCharsets.UTF_8);
            log.debug("Wrote project '{}' to {}", project.meta().name(), path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write .nfx file: " + path, e);
        }
    }

    // ---------------------------------------------------------------
    // TOML serialization
    // ---------------------------------------------------------------

    static String serialize(Project project) {
        var sb = new StringBuilder(512);

        // Version
        sb.append("version = ").append(quote(project.version())).append('\n');

        // [project] — persistent UUID
        sb.append("\n[project]\n");
        sb.append("id = ").append(quote(project.id())).append('\n');

        // [meta]
        sb.append("\n[meta]\n");
        Meta meta = project.meta();
        sb.append("name = ").append(quote(meta.name())).append('\n');
        sb.append("author = ").append(quote(meta.author())).append('\n');

        // [function]
        sb.append("\n[function]\n");
        var func = project.function();
        sb.append("x = ").append(quote(func.xExpression())).append('\n');
        sb.append("y = ").append(quote(func.yExpression())).append('\n');
        sb.append("z = ").append(quote(func.zExpression())).append('\n');
        sb.append("start = ").append(doubleStr(func.start())).append('\n');
        sb.append("end = ").append(doubleStr(func.end())).append('\n');
        sb.append("step = ").append(doubleStr(func.step())).append('\n');

        // [particle]
        ParticleSettings particle = project.particle();
        sb.append("\n[particle]\n");
        sb.append("size = ").append(doubleStr(particle.size())).append('\n');
        sb.append("density = ").append(doubleStr(particle.density())).append('\n');

        // [render]
        RenderSettings render = project.render();
        sb.append("\n[render]\n");
        sb.append("grid = ").append(render.grid()).append('\n');
        sb.append("axis = ").append(render.axis()).append('\n');

        // [parameter]
        Map<String, Double> params = project.parameters();
        if (params != null && !params.isEmpty()) {
            sb.append("\n[parameter]\n");
            for (var entry : params.entrySet()) {
                sb.append(entry.getKey()).append(" = ").append(doubleStr(entry.getValue())).append('\n');
            }
        }

        return sb.toString();
    }

    /**
     * Wraps a string in double quotes with proper TOML escaping.
     */
    static String quote(String value) {
        var sb = new StringBuilder(value.length() + 2);
        sb.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        sb.append('"');
        return sb.toString();
    }

    /**
     * Formats a double value without unnecessary trailing zeros.
     */
    static String doubleStr(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}
