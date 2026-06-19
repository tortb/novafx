package com.novafx.export;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.novafx.core.domain.Project;
import com.novafx.math.FunctionDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Exports and imports NovaFX projects in the compressed {@code .nfxz} format.
 * <p>
 * An {@code .nfxz} file is a ZIP archive containing:
 * <ul>
 *   <li>{@code project.nfx} — the project data in standard NovaFX JSON format</li>
 *   <li>{@code thumbnail.png} — optional preview image (not yet supported in V1)</li>
 * </ul>
 */
public final class NfxzExporter {

    private static final Logger log = LoggerFactory.getLogger(NfxzExporter.class);

    private static final String NFX_ENTRY = "project.nfx";
    private static final String VERSION = "1.0";
    private static final JsonMapper MAPPER = (JsonMapper) JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .build();

    private NfxzExporter() {
    }

    /**
     * Exports a project as a compressed {@code .nfxz} file.
     *
     * @param project the project to export
     * @param output  the output file path (should end in {@code .nfxz})
     */
    public static void export(Project project, Path output) {
        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(output)))) {
            // Write project.nfx entry
            zos.putNextEntry(new ZipEntry(NFX_ENTRY));
            byte[] projectBytes = serializeProject(project);
            zos.write(projectBytes);
            zos.closeEntry();

            log.info("Exported .nfxz to {} ({} bytes)", output, projectBytes.length);
        } catch (IOException e) {
            throw new RuntimeException("Failed to export .nfxz to " + output, e);
        }
    }

    /**
     * Imports a project from a compressed {@code .nfxz} file.
     *
     * @param input the .nfxz file to import
     * @return the imported project
     */
    public static Project importProject(Path input) {
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(Files.newInputStream(input)))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (NFX_ENTRY.equals(entry.getName())) {
                    byte[] data = zis.readAllBytes();
                    zis.closeEntry();
                    Project project = deserializeProject(data);
                    log.info("Imported .nfxz from {}: '{}'", input, project.name());
                    return project;
                }
                zis.closeEntry();
            }
            throw new RuntimeException("Invalid .nfxz file: missing " + NFX_ENTRY);
        } catch (IOException e) {
            throw new RuntimeException("Failed to import .nfxz from " + input, e);
        }
    }

    // ---------------------------------------------------------------
    // JSON serialization (matches .nfx format)
    // ---------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static byte[] serializeProject(Project project) throws IOException {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("version", VERSION);
        root.put("id", project.id().toString());
        root.put("name", project.name());
        root.put("description", project.description());
        root.put("createdAt", project.createdAt().toString());
        root.put("updatedAt", project.updatedAt().toString());

        FunctionDefinition def = project.functionDefinition();
        Map<String, Object> func = new LinkedHashMap<>();
        func.put("x", def.xExpression());
        func.put("y", def.yExpression());
        func.put("z", def.zExpression());
        func.put("start", def.start());
        func.put("end", def.end());
        func.put("step", def.step());
        root.put("function", func);

        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsBytes(root);
    }

    private static Project deserializeProject(byte[] data) throws IOException {
        Map<String, Object> root = MAPPER.readValue(data, Map.class);

        UUID id = UUID.fromString((String) root.get("id"));
        String name = (String) root.get("name");
        String description = (String) root.getOrDefault("description", "");
        Instant createdAt = Instant.parse((String) root.get("createdAt"));
        Instant updatedAt = Instant.parse((String) root.get("updatedAt"));

        Map<String, Object> func = (Map<String, Object>) root.get("function");
        String x = (String) func.get("x");
        String y = (String) func.get("y");
        String z = (String) func.get("z");
        double start = asDouble(func.get("start"));
        double end = asDouble(func.get("end"));
        double step = asDouble(func.get("step"));

        FunctionDefinition definition = new FunctionDefinition(x, y, z, start, end, step);
        return new Project(id, name, description, definition, createdAt, updatedAt);
    }

    private static double asDouble(Object value) {
        if (value instanceof Number n) return n.doubleValue();
        return Double.parseDouble(value.toString());
    }
}
