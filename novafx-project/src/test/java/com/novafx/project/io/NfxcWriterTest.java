package com.novafx.project.io;

import com.novafx.project.ProjectFormatException;
import com.novafx.project.model.CompiledPointCloud;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link NfxcWriter} binary serialization.
 */
class NfxcWriterTest {

    private final NfxcWriter writer = new NfxcWriter();

    @TempDir
    Path tempDir;

    @Test
    void shouldWriteSinglePoint() throws IOException {
        float[] data = {1.0f, 2.0f, 3.0f};
        var cloud = new CompiledPointCloud(data, 1, 0, 0, 0, 10, 10, 10);
        Path file = tempDir.resolve("single.nfxc");

        writer.write(cloud, file);

        assertThat(file).isRegularFile();
        assertThat(Files.size(file)).isEqualTo(36L + 12L); // header + 1 point
    }

    @Test
    void shouldWriteMultiplePoints() throws IOException {
        int n = 1000;
        float[] data = new float[n * 3];
        for (int i = 0; i < n; i++) {
            data[i * 3] = i;
            data[i * 3 + 1] = i * 2;
            data[i * 3 + 2] = i * 3;
        }
        var cloud = CompiledPointCloud.from(
                java.util.stream.IntStream.range(0, n)
                        .mapToObj(i -> new com.novafx.math.Vector3d(i, i * 2, i * 3))
                        .toList());
        Path file = tempDir.resolve("many.nfxc");

        writer.write(cloud, file);

        assertThat(file).isRegularFile();
        long expectedSize = 36L + (long) n * 12;
        assertThat(Files.size(file)).isEqualTo(expectedSize);
    }

    @Test
    void shouldRejectDataSizeMismatch() {
        float[] data = {1.0f, 2.0f}; // 2 floats, but pointCount=1 needs 3
        var cloud = new CompiledPointCloud(data, 1, 0, 0, 0, 1, 1, 1);

        assertThatThrownBy(() -> writer.write(cloud, tempDir.resolve("bad.nfxc")))
                .isInstanceOf(ProjectFormatException.class)
                .hasMessageContaining("length mismatch");
    }

    @Test
    void shouldWriteOneMillionPoints() throws IOException {
        int n = 1_000_000;
        float[] data = new float[n * 3];
        for (int i = 0; i < n; i++) {
            double t = (double) i / n * 10;
            data[i * 3] = (float) Math.cos(t);
            data[i * 3 + 1] = (float) Math.sin(t);
            data[i * 3 + 2] = (float) (t * 0.1);
        }
        var cloud = new CompiledPointCloud(data, n,
                -1, -1, 0, 1, 1, 1);
        Path file = tempDir.resolve("million.nfxc");

        writer.write(cloud, file);

        assertThat(file).isRegularFile();
        assertThat(Files.size(file)).isGreaterThan(1_000_000L);
    }
}
