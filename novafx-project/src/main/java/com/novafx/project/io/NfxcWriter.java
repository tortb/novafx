package com.novafx.project.io;

import com.novafx.project.ProjectFormatException;
import com.novafx.project.model.CompiledPointCloud;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Writes {@code .nfxc} compiled files in little-endian binary format.
 * <p>
 * Layout:
 * <pre>
 * [Magic: "NFXC"  4 bytes]
 * [Version: int32  4 bytes]
 * [PointCount: int32  4 bytes]
 * [BoundingBox: 6×float32  24 bytes]
 * [PointBuffer: pointCount × 3 × float32]
 * </pre>
 */
public final class NfxcWriter {

    private static final Logger log = LoggerFactory.getLogger(NfxcWriter.class);
    private static final byte[] MAGIC = {'N', 'F', 'X', 'C'};
    private static final int VERSION = 1;
    private static final int HEADER_SIZE = 36; // 4 + 4 + 4 + 24

    /**
     * Writes a compiled point cloud to the given path.
     *
     * @param cloud the point cloud; must not be null
     * @param path  the output path for the {@code .nfxc} file
     * @throws ProjectFormatException if the data is invalid or writing fails
     */
    public void write(CompiledPointCloud cloud, Path path) {
        if (cloud.points().length != cloud.pointCount() * 3) {
            throw new ProjectFormatException(
                    "Point data length mismatch: expected " + (cloud.pointCount() * 3)
                            + " floats, got " + cloud.points().length);
        }

        int bufferSize = HEADER_SIZE + cloud.pointCount() * 12;
        ByteBuffer buf = ByteBuffer.allocate(bufferSize);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        // Header
        buf.put(MAGIC);
        buf.putInt(VERSION);
        buf.putInt(cloud.pointCount());

        // Bounding box
        buf.putFloat(cloud.minX());
        buf.putFloat(cloud.minY());
        buf.putFloat(cloud.minZ());
        buf.putFloat(cloud.maxX());
        buf.putFloat(cloud.maxY());
        buf.putFloat(cloud.maxZ());

        // Point buffer (already interleaved x,y,z)
        var floatBuf = buf.asFloatBuffer();
        floatBuf.put(cloud.points(), 0, cloud.points().length);
        buf.position(buf.position() + cloud.points().length * 4);

        buf.flip();

        try (OutputStream os = Files.newOutputStream(path, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
             WritableByteChannel channel = Channels.newChannel(os)) {
            channel.write(buf);
        } catch (IOException e) {
            throw new ProjectFormatException("Failed to write .nfxc file: " + path, e);
        }

        log.debug("Wrote {} points to {}", cloud.pointCount(), path);
    }
}
