package com.novafx.project.io;

import com.novafx.project.ProjectFormatException;
import com.novafx.project.model.CompiledPointCloud;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads {@code .nfxc} compiled files in little-endian binary format.
 * <p>
 * Validates the magic bytes and version before parsing.
 */
public final class NfxcReader {

    private static final Logger log = LoggerFactory.getLogger(NfxcReader.class);
    private static final byte[] MAGIC = {'N', 'F', 'X', 'C'};
    private static final int VERSION = 1;
    private static final int MIN_FILE_SIZE = 36; // header only, no points

    /**
     * Reads a compiled point cloud from a {@code .nfxc} file.
     *
     * @param path the path to the .nfxc file; must not be null
     * @return the parsed point cloud
     * @throws ProjectFormatException if the file is corrupted, has an
     *         invalid magic/version, or cannot be read
     */
    public CompiledPointCloud read(Path path) {
        byte[] fileBytes;
        try {
            fileBytes = Files.readAllBytes(path);
        } catch (IOException e) {
            throw new ProjectFormatException("Failed to read .nfxc file: " + path, e);
        }

        if (fileBytes.length < MIN_FILE_SIZE) {
            throw new ProjectFormatException(
                    "File too small to be a valid .nfxc file: " + fileBytes.length + " bytes");
        }

        ByteBuffer buf = ByteBuffer.wrap(fileBytes).order(ByteOrder.LITTLE_ENDIAN);

        // Validate magic
        byte[] magic = new byte[4];
        buf.get(magic);
        for (int i = 0; i < 4; i++) {
            if (magic[i] != MAGIC[i]) {
                throw new ProjectFormatException(
                        "Invalid magic bytes: expected 'NFXC', got '"
                                + new String(magic) + "'");
            }
        }

        // Validate version
        int version = buf.getInt();
        if (version != VERSION) {
            throw new ProjectFormatException(
                    "Unsupported version: " + version + "; expected " + VERSION);
        }

        // Read point count
        int pointCount = buf.getInt();
        if (pointCount < 0) {
            throw new ProjectFormatException("Negative point count: " + pointCount);
        }

        // Read bounding box
        float minX = buf.getFloat();
        float minY = buf.getFloat();
        float minZ = buf.getFloat();
        float maxX = buf.getFloat();
        float maxY = buf.getFloat();
        float maxZ = buf.getFloat();

        // Read point buffer
        int expectedFloatCount = pointCount * 3;
        int remainingFloats = buf.remaining() / 4;
        if (remainingFloats < expectedFloatCount) {
            throw new ProjectFormatException(
                    "Truncated point data: expected " + expectedFloatCount
                            + " floats, got " + remainingFloats);
        }

        float[] points = new float[expectedFloatCount];
        for (int i = 0; i < expectedFloatCount; i++) {
            points[i] = buf.getFloat();
        }

        log.debug("Read {} points from {}", pointCount, path);

        return new CompiledPointCloud(
                points, pointCount,
                minX, minY, minZ, maxX, maxY, maxZ
        );
    }
}
