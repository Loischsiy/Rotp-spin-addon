package com.loischsiy.rotpspin.client.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SpinSphereTest {
    private static final double EPS = 1e-6;

    @Test
    void counts() {
        SpinSphere sphere = SpinSphere.build(1.0F, 24, 16);
        assertEquals(25 * 17, sphere.vertexCount());
        assertEquals(24 * 16 * 2, sphere.triangleCount());
        assertEquals(sphere.vertexCount() * 3, sphere.positions.length);
        assertEquals(sphere.vertexCount() * 3, sphere.normals.length);
        assertEquals(sphere.vertexCount() * 2, sphere.uvs.length);
    }

    @Test
    void verticesLieOnSphereAndNormalsAreUnit() {
        SpinSphere sphere = SpinSphere.build(0.5F, 12, 8);
        for (int v = 0; v < sphere.vertexCount(); v++) {
            double x = sphere.positions[v * 3];
            double y = sphere.positions[v * 3 + 1];
            double z = sphere.positions[v * 3 + 2];
            assertEquals(0.5, Math.sqrt(x * x + y * y + z * z), EPS);
            double nx = sphere.normals[v * 3];
            double ny = sphere.normals[v * 3 + 1];
            double nz = sphere.normals[v * 3 + 2];
            assertEquals(1.0, Math.sqrt(nx * nx + ny * ny + nz * nz), EPS);
            // Normal points the same way as the position (outward).
            assertTrue(x * nx + y * ny + z * nz > 0);
            assertTrue(sphere.uvs[v * 2] >= 0 && sphere.uvs[v * 2] <= 1);
            assertTrue(sphere.uvs[v * 2 + 1] >= 0 && sphere.uvs[v * 2 + 1] <= 1);
        }
    }

    @Test
    void indicesReferenceRealVertices() {
        SpinSphere sphere = SpinSphere.build(1.0F, 8, 6);
        for (int index : sphere.indices) {
            assertTrue(index >= 0 && index < sphere.vertexCount());
        }
    }

    @Test
    void spinAxisIsPerpendicularAndUnit() {
        // Flying south (+Z): the wheel axle points east-west.
        net.minecraft.util.math.vector.Vector3f axis = SteelBallRenderer.spinAxis(0, 1);
        assertEquals(-1, axis.x(), 1e-6);
        assertEquals(0, axis.y(), 1e-6);
        assertEquals(0, axis.z(), 1e-6);
        // Flying diagonally: still horizontal and unit.
        net.minecraft.util.math.vector.Vector3f diag = SteelBallRenderer.spinAxis(1, 1);
        assertEquals(0, diag.y(), 1e-6);
        assertEquals(1, Math.sqrt(diag.x() * diag.x() + diag.z() * diag.z()), 1e-6);
        // Straight up/down: fallback axis.
        net.minecraft.util.math.vector.Vector3f fallback = SteelBallRenderer.spinAxis(0, 0);
        assertEquals(1, fallback.x(), 1e-6);
    }
}
