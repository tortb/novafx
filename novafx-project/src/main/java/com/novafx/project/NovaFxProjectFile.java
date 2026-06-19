package com.novafx.project;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.novafx.math.FunctionDefinition;

import java.time.Instant;
import java.util.UUID;

/**
 * JSON-serializable representation of a NovaFX project file ({@code .nfx}).
 * <p>
 * This is a data transfer object that maps directly to the on-disk format.
 * It combines project metadata with a parametric function definition.
 */
final class NovaFxProjectFile {

    private String version;
    private UUID id;
    private String name;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private FunctionData function;

    /** Default constructor for Jackson deserialization. */
    public NovaFxProjectFile() {
    }

    NovaFxProjectFile(String version, UUID id, String name, String description,
                      Instant createdAt, Instant updatedAt, FunctionData function) {
        this.version = version;
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.function = function;
    }

    // ---- Getters / Setters for Jackson ----

    @JsonProperty("version")
    public String version() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("id")
    public UUID id() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @JsonProperty("name")
    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("description")
    public String description() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("createdAt")
    public Instant createdAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @JsonProperty("updatedAt")
    public Instant updatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @JsonProperty("function")
    public FunctionData function() {
        return function;
    }

    public void setFunction(FunctionData function) {
        this.function = function;
    }

    // ---------------------------------------------------------------
    // Nested DTO for the function definition
    // ---------------------------------------------------------------

    static final class FunctionData {

        private String x;
        private String y;
        private String z;
        private double start;
        private double end;
        private double step;

        public FunctionData() {
        }

        FunctionData(String x, String y, String z, double start, double end, double step) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.start = start;
            this.end = end;
            this.step = step;
        }

        @JsonProperty("x")
        public String x() {
            return x;
        }

        public void setX(String x) {
            this.x = x;
        }

        @JsonProperty("y")
        public String y() {
            return y;
        }

        public void setY(String y) {
            this.y = y;
        }

        @JsonProperty("z")
        public String z() {
            return z;
        }

        public void setZ(String z) {
            this.z = z;
        }

        @JsonProperty("start")
        public double start() {
            return start;
        }

        public void setStart(double start) {
            this.start = start;
        }

        @JsonProperty("end")
        public double end() {
            return end;
        }

        public void setEnd(double end) {
            this.end = end;
        }

        @JsonProperty("step")
        public double step() {
            return step;
        }

        public void setStep(double step) {
            this.step = step;
        }

        FunctionDefinition toFunctionDefinition() {
            return new FunctionDefinition(x, y, z, start, end, step);
        }

        static FunctionData from(FunctionDefinition def) {
            return new FunctionData(
                    def.xExpression(), def.yExpression(), def.zExpression(),
                    def.start(), def.end(), def.step()
            );
        }
    }
}
