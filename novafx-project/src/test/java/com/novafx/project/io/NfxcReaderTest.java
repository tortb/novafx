package com.novafx.project.io;

import com.novafx.project.ProjectFormatException;
import com.novafx.project.model.CompiledPointCloud;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link NfxcReader} binary parsing.
 */
class NfxcReaderTest {

    private final NfxcWriter writer = new NfxcWriter();
    private final NfxcReader reader = new NfxcReader();

    @TempDir
    Path tempDir;

    @Test
    void shouldReadSinglePoint() {
        float[] data = {1.5f, 2.5f, 3.5f};
        var cloud = new CompiledPointCloud(data, 1, 1, 2, 3, 2, 3, 4);
        Path file = tempDir.resolve("single.nfxc");
        writer.write(cloud, file);

        CompiledPointCloud result = reader.read(file);

        assertThat(result.pointCount()).isEqualTo(1);
        assertThat(result.points()).containsExactly(1.5f, 2.5f, 3.5f);
        assertThat(result.minX()).isEqualTo(1.0f);
        assertThat(result.maxZ()).isEqualTo(4.0f);
    }

    @Test
    void shouldReadMultiplePoints() {
        int n = 100;
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

        CompiledPointCloud result = reader.read(file);

        assertThat(result.pointCount()).isEqualTo(n);
        assertThat(result.points()).hasSize(n * 3);
        assertThat(result.points()[0]).isEqualTo(0f);
        assertThat(result.points()[1]).isEqualTo(0f);
        assertThat(result.points()[n * 3 - 1]).isEqualTo((n - 1) * 3f);
    }

    @Test
    void shouldRejectInvalidMagic() throws Exception {
        // Write a 36-byte file with bad magic
        ByteBuffer buf = ByteBuffer.allocate(36);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.put(new byte[]{'X', 'X', 'X', 'X'});
        buf.putInt(1); // version
        buf.putInt(0); // point count
        buf.putFloat(0).putFloat(0).putFloat(0).putFloat(0).putFloat(0).putFloat(0);
        Path file = tempDir.resolve("badmagic.nfxc");
        Files.write(file, buf.array());

        assertThatThrownBy(() -> reader.read(file))
                .isInstanceOf(ProjectFormatException.class)
                .hasMessageContaining("magic");
    }

    @Test
    void shouldRejectUnsupportedVersion() throws Exception {
        ByteBuffer buf = ByteBuffer.allocate(36);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.put(new byte[]{'N', 'F', 'X', 'C'});
        buf.putInt(99); // unsupported version
        buf.putInt(0);
        buf.putFloat(0).putFloat(0).putFloat(0).putFloat(0).putFloat(0).putFloat(0);
        Path file = tempDir.resolve("badver.nfxc");
        Files.write(file, buf.array());

        assertThatThrownBy(() -> reader.read(file))
                .isInstanceOf(ProjectFormatException.class)
                .hasMessageContaining("version");
    }

    @Test
    void shouldRejectTruncatedFile() throws Exception {
        Path file = tempDir.resolve("truncated.nfxc");
        Files.write(file, new byte[]{'N', 'F', 'X', 'C', 0, 0, 0, 1}); // only 8 bytes

        assertThatThrownBy(() -> reader.read(file))
                .isInstanceOf(ProjectFormatException.class)
                .hasMessageContaining("too small");
    }

    @Test
    void shouldRejectNegativePointCount() throws Exception {
        ByteBuffer buf = ByteBuffer.allocate(36);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.put(new byte[]{'N', 'F', 'X', 'C'});
        buf.putInt(1);
        buf.putInt(-1); // negative count
        buf.putFloat(0).putFloat(0).putFloat(0).putFloat(0).putFloat(0).putFloat(0);
        Path file = tempDir.resolve("neg.nfxc");
        Files.write(file, buf.array());

        assertThatThrownBy(() -> reader.read(file))
                .isInstanceOf(ProjectFormatException.class)
                .hasMessageContaining("Negative");
    }

    @Test
    void shouldReadOneMillionPoints() {
        int n = 1_000_000;
        float[] data = new float[n * 3];
        for (int i = 0; i < n; i++) {
            double t = (double) i / n * 10;
            data[i * 3] = (float) Math.cos(t);
            data[i * 3 + 1] = (float) Math.sin(t);
            data[i * 3 + 2] = (float) (t * 0.1);
        }
        var cloud = new CompiledPointCloud(data, n, -1, -1, 0, 1, 1, 1);
        Path file = tempDir.resolve("million.nfxc");
        writer.write(cloud, file);

        CompiledPointCloud result = reader.read(file);

        assertThat(result.pointCount()).isEqualTo(n);
        assertThat(result.points()).hasSize(n * 3);
    }

    @Test
    void shouldRoundTripWithWriter() {
        var cloud = CompiledPointCloud.from(
                java.util.List.of(
                        new com.novafx.math.Vector3d(1.1, 2.2, 3.3),
                        new com.novafx.math.Vector3d(4.4, 5.5, 6.6),
                        new com.novafx.math.Vector3d(7.7, 8.8, 9.9)
                ));
        Path file = tempDir.resolve("roundtrip.nfxc");
        writer.write(cloud, file);

        CompiledPointCloud result = reader.read(file);

        assertThat(result.pointCount()).isEqualTo(3);
        assertThat(result.points()[0]).isEqualTo(1.1f);
        assertThat(result.points()[4]).isEqualTo(5.5f);
        assertThat(result.points()[8]).isEqualTo(9.9f);
    }
}
