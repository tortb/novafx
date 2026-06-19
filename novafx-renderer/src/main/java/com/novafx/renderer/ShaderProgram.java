package com.novafx.renderer;

import static org.lwjgl.opengl.GL33.*;

/**
 * An OpenGL shader program composed of a vertex and a fragment shader.
 * <p>
 * Shader source is provided as Java strings. The program is compiled
 * and linked on construction. Call {@link #use()} to activate it for
 * rendering and {@link #cleanup()} to release GPU resources.
 */
public final class ShaderProgram {

    private final int programId;

    /**
     * Compiles and links a shader program from vertex and fragment source.
     *
     * @param vertexSource   GLSL vertex shader source
     * @param fragmentSource GLSL fragment shader source
     * @throws IllegalStateException if compilation or linking fails
     */
    public ShaderProgram(String vertexSource, String fragmentSource) {
        int vertexId = compileShader(vertexSource, GL_VERTEX_SHADER);
        int fragmentId = compileShader(fragmentSource, GL_FRAGMENT_SHADER);
        this.programId = linkProgram(vertexId, fragmentId);
        glDeleteShader(vertexId);
        glDeleteShader(fragmentId);
    }

    /**
     * Activates this shader program for rendering.
     */
    public void use() {
        glUseProgram(programId);
    }

    /**
     * Deactivates the current shader program.
     */
    public static void unbind() {
        glUseProgram(0);
    }

    /**
     * Returns the location of a uniform variable.
     *
     * @param name uniform variable name
     * @return uniform location, or -1 if not found
     */
    public int getUniformLocation(String name) {
        return glGetUniformLocation(programId, name);
    }

    /**
     * Sets a 4x4 float matrix uniform.
     *
     * @param location uniform location
     * @param matrix   column-major 4x4 matrix values (16 floats)
     */
    public void setUniformMatrix4(int location, float[] matrix) {
        glUniformMatrix4fv(location, false, matrix);
    }

    /**
     * Sets a vec3 uniform.
     *
     * @param location uniform location
     * @param x        x-component
     * @param y        y-component
     * @param z        z-component
     */
    public void setUniformVec3(int location, float x, float y, float z) {
        glUniform3f(location, x, y, z);
    }

    /**
     * Sets a float uniform.
     *
     * @param location uniform location
     * @param value    float value
     */
    public void setUniformFloat(int location, float value) {
        glUniform1f(location, value);
    }

    /**
     * Deletes the shader program from the GPU.
     */
    public void cleanup() {
        glDeleteProgram(programId);
    }

    private static int compileShader(String source, int type) {
        int shaderId = glCreateShader(type);
        glShaderSource(shaderId, source);
        glCompileShader(shaderId);
        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(shaderId);
            glDeleteShader(shaderId);
            throw new IllegalStateException("Shader compilation failed: " + log);
        }
        return shaderId;
    }

    private static int linkProgram(int vertexId, int fragmentId) {
        int id = glCreateProgram();
        glAttachShader(id, vertexId);
        glAttachShader(id, fragmentId);
        glLinkProgram(id);
        if (glGetProgrami(id, GL_LINK_STATUS) == GL_FALSE) {
            String log = glGetProgramInfoLog(id);
            glDeleteProgram(id);
            throw new IllegalStateException("Program linking failed: " + log);
        }
        return id;
    }
}
