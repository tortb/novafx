package com.novafx.project.model;

import com.novafx.math.Vector3d;

import java.util.List;

/**
 * A compiled point cloud produced by the {@code ProjectCompiler}.
 * <p>
 * Contains an array of 3D points (as interleaved {@code float} values
 * for GPU upload) together with the bounding box metadata.
 *
 * @param points      interleaved float array: {@code [x0, y0, z0, x1, y1, z1, …]};
 *                    length must be {@code pointCount * 3}
 * @param pointCount  the number of points
 * @param minX        minimum X coordinate
 * @param minY        minimum Y coordinate
 * @param minZ        minimum Z coordinate
 * @param maxX        maximum X coordinate
 * @param maxY        maximum Y coordinate
 * @param maxZ        maximum Z coordinate
 */
public record CompiledPointCloud(
        float[] points,
        int pointCount,
        float minX, float minY, float minZ,
        float maxX, float maxY, float maxZ
) {

    /**
     * Creates a CompiledPointCloud from a list of Vector3d.
     *
     * @param vecs the source points; must not be null
     * @return a new CompiledPointCloud with computed bounding box
     */
    public static CompiledPointCloud from(List<Vector3d> vecs) {
        int n = vecs.size();
        float[] data = new float[n * 3];
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;

        for (int i = 0; i < n; i++) {
            Vector3d v = vecs.get(i);
            float x = (float) v.x();
            float y = (float) v.y();
            float z = (float) v.z();
            data[i * 3] = x;
            data[i * 3 + 1] = y;
            data[i * 3 + 2] = z;
            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (z < minZ) minZ = z;
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
            if (z > maxZ) maxZ = z;
        }

        return new CompiledPointCloud(data, n,
                minX, minY, minZ, maxX, maxY, maxZ);
    }
}
