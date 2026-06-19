package com.novafx.project.io;

import com.novafx.math.FunctionDefinition;
import com.novafx.project.ProjectFormatException;
import com.novafx.project.model.Meta;
import com.novafx.project.model.ParticleSettings;
import com.novafx.project.model.Project;
import com.novafx.project.model.RenderSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads {@code .nfx} files using the Tomlj parser.
 * <p>
 * Produces a file-format {@link Project} model without applying any
 * business logic. Validates the TOML structure and file format version.
 */
public final class NfxReader implements ProjectReader {

    private static final Logger log = LoggerFactory.getLogger(NfxReader.class);

    @Override
    public Project read(Path path) {
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            if (content.isEmpty()) {
                throw new ProjectFormatException("File is empty: " + path);
            }

            // Strip UTF-8 BOM if present
            if (content.charAt(0) == '﻿') {
                content = content.substring(1);
            }

            TomlParseResult result = Toml.parse(content);
            if (result.hasErrors()) {
                var err = result.errors().get(0);
                throw new ProjectFormatException(
                        "TOML parse error at line " + err.position().line()
                                + ": " + err.getMessage());
            }

            String version = result.getString("version");
            if (version == null || version.isBlank()) {
                throw new ProjectFormatException("Missing or empty 'version' field in " + path);
            }
            if (!Project.CURRENT_VERSION.equals(version)) {
                throw new ProjectFormatException(
                        "Unsupported version '" + version + "'; expected " + Project.CURRENT_VERSION);
            }

            // ── [project] ──
            String projectId = null;
            var projectTable = result.getTable("project");
            if (projectTable != null) {
                projectId = projectTable.getString("id");
            }

            // ── [meta] ──
            String name = "";
            String author = "";
            var metaTable = result.getTable("meta");
            if (metaTable != null) {
                name = getStringOrDefault(metaTable, "name", "");
                author = getStringOrDefault(metaTable, "author", "");
            }

            // ── [function] ──
            var funcTable = result.getTable("function");
            if (funcTable == null) {
                throw new ProjectFormatException("Missing [function] section in " + path);
            }
            String xExpr = getStringOrDefault(funcTable, "x", "t");
            String yExpr = getStringOrDefault(funcTable, "y", "t");
            String zExpr = getStringOrDefault(funcTable, "z", "0");
            double start = getDoubleOrDefault(funcTable, "start", 0.0);
            double end = getDoubleOrDefault(funcTable, "end", 10.0);
            double step = getDoubleOrDefault(funcTable, "step", 0.1);

            // ── [particle] ──
            double pSize = ParticleSettings.DEFAULT_SIZE;
            double density = ParticleSettings.DEFAULT_DENSITY;
            var particleTable = result.getTable("particle");
            if (particleTable != null) {
                pSize = getDoubleOrDefault(particleTable, "size", ParticleSettings.DEFAULT_SIZE);
                density = getDoubleOrDefault(particleTable, "density", ParticleSettings.DEFAULT_DENSITY);
            }

            // ── [render] ──
            boolean grid = true;
            boolean axis = true;
            var renderTable = result.getTable("render");
            if (renderTable != null) {
                grid = getBooleanOrDefault(renderTable, "grid", true);
                axis = getBooleanOrDefault(renderTable, "axis", true);
            }

            FunctionDefinition function;
            try {
                function = new FunctionDefinition(xExpr, yExpr, zExpr, start, end, step);
            } catch (Exception e) {
                throw new ProjectFormatException("Invalid function definition in " + path, e);
            }

            var project = new Project(
                    version,
                    projectId,
                    new Meta(name, author),
                    function,
                    new ParticleSettings(pSize, density),
                    new RenderSettings(grid, axis)
            );

            log.debug("Read project '{}' from {}", project.meta().name(), path);
            return project;

        } catch (ProjectFormatException e) {
            throw e;
        } catch (IOException e) {
            throw new ProjectFormatException("Failed to read file: " + path, e);
        }
    }

    // ---------------------------------------------------------------
    // Tomlj helper methods — the library uses Supplier-based defaults
    // ---------------------------------------------------------------

    private static String getStringOrDefault(org.tomlj.TomlTable table, String key, String defaultValue) {
        String value = table.getString(key);
        return value != null ? value : defaultValue;
    }

    private static double getDoubleOrDefault(org.tomlj.TomlTable table, String key, double defaultValue) {
        // Tomlj throws TomlInvalidType if the value is an integer;
        // we catch that and try getLong instead.
        try {
            Double value = table.getDouble(key);
            if (value != null) return value;
        } catch (Exception ignored) {
        }
        Long longValue = table.getLong(key);
        if (longValue != null) return longValue.doubleValue();
        return defaultValue;
    }

    private static boolean getBooleanOrDefault(org.tomlj.TomlTable table, String key, boolean defaultValue) {
        Boolean value = table.getBoolean(key);
        return value != null ? value : defaultValue;
    }
}
