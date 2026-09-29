package com.loischsiy.rotpspin.client.render;

/**
 * Exact UV-sphere mesh (no Blockbench needed: a ball is pure math, smoother than any
 * cube-built model). Shared by all spinning balls; the renderer only scales and rotates it.
 * Pure data, no World access.
 */
public final class SpinSphere {
    public final float[] positions;
    public final float[] normals;
    public final float[] uvs;
    public final int[] indices;

    private SpinSphere(float[] positions, float[] normals, float[] uvs, int[] indices) {
        this.positions = positions;
        this.normals = normals;
        this.uvs = uvs;
        this.indices = indices;
    }

    /** Builds a sphere of {@code radius} with {@code longitudes} x {@code latitudes} segments. */
    public static SpinSphere build(float radius, int longitudes, int latitudes) {
        int cols = longitudes + 1;
        int rows = latitudes + 1;
        float[] positions = new float[cols * rows * 3];
        float[] normals = new float[cols * rows * 3];
        float[] uvs = new float[cols * rows * 2];
        for (int lat = 0; lat < rows; lat++) {
            double theta = Math.PI * lat / latitudes;
            double sinTheta = Math.sin(theta);
            double cosTheta = Math.cos(theta);
            for (int lon = 0; lon < cols; lon++) {
                double phi = 2.0 * Math.PI * lon / longitudes;
                float x = (float) (sinTheta * Math.cos(phi));
                float y = (float) cosTheta;
                float z = (float) (sinTheta * Math.sin(phi));
                int v = lat * cols + lon;
                positions[v * 3] = x * radius;
                positions[v * 3 + 1] = y * radius;
                positions[v * 3 + 2] = z * radius;
                normals[v * 3] = x;
                normals[v * 3 + 1] = y;
                normals[v * 3 + 2] = z;
                uvs[v * 2] = (float) lon / longitudes;
                uvs[v * 2 + 1] = (float) lat / latitudes;
            }
        }
        int[] indices = new int[longitudes * latitudes * 6];
        int i = 0;
        for (int lat = 0; lat < latitudes; lat++) {
            for (int lon = 0; lon < longitudes; lon++) {
                int a = lat * cols + lon;
                int b = a + 1;
                int c = a + cols;
                int d = c + 1;
                indices[i++] = a;
                indices[i++] = c;
                indices[i++] = b;
                indices[i++] = b;
                indices[i++] = c;
                indices[i++] = d;
            }
        }
        return new SpinSphere(positions, normals, uvs, indices);
    }

    public int vertexCount() {
        return positions.length / 3;
    }

    public int triangleCount() {
        return indices.length / 3;
    }
}
