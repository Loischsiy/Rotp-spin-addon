package com.loischsiy.rotpspin.power;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BallBreakerAuraTest {
    private static final double EPS = 1e-9;

    @Test
    void sparkCountFollowsSummonFade() {
        assertEquals(0, BallBreakerAura.sparkCount(6, 0F));
        assertEquals(3, BallBreakerAura.sparkCount(6, 0.5F));
        assertEquals(6, BallBreakerAura.sparkCount(6, 1F));
        assertEquals(6, BallBreakerAura.sparkCount(6, 2F));
        assertEquals(0, BallBreakerAura.sparkCount(0, 1F));
        assertEquals(0, BallBreakerAura.sparkCount(-3, 1F));
    }

    @Test
    void verticalRadiusWrapsTheBody() {
        assertEquals(1.25, BallBreakerAura.verticalRadius(1.9, 0.6), EPS);
        assertEquals(0.3, BallBreakerAura.verticalRadius(-1.0, 0.6), EPS);
    }

    @Test
    void shellPointsLieOnTheEllipsoid() {
        double r = 0.6, h = 1.25;
        for (double u = 0.0; u < 1.0; u += 0.13) {
            for (double v = 0.0; v <= 1.0; v += 0.17) {
                double[] p = BallBreakerAura.shellPoint(u, v, r, h);
                double norm = (p[0] * p[0] + p[2] * p[2]) / (r * r) + (p[1] * p[1]) / (h * h);
                assertEquals(1.0, norm, 1e-6);
            }
        }
        assertArrayEquals(new double[] {0.0, h, 0.0}, BallBreakerAura.shellPoint(0.0, 0.0, r, h), EPS);
        assertEquals(-h, BallBreakerAura.shellPoint(0.3, 1.0, r, h)[1], EPS);
    }

    @Test
    void arcKeepsEndpointsAndStaysWithinJitter() {
        double[] from = {0.0, 0.0, 0.0};
        double[] to = {1.0, 0.5, -1.0};
        int segments = 5;
        double jitter = 0.12;
        double[] randoms = {0.0, 1.0, 0.5, 1.0, 0.0, 0.25, 0.9, 0.1, 0.6, 0.0, 0.0, 1.0};
        double[][] arc = BallBreakerAura.arc(from, to, segments, jitter, randoms);
        assertEquals(segments + 1, arc.length);
        assertArrayEquals(from, arc[0], EPS);
        assertArrayEquals(to, arc[segments], EPS);
        for (int i = 1; i < segments; i++) {
            double t = (double) i / segments;
            for (int axis = 0; axis < 3; axis++) {
                double straight = from[axis] + (to[axis] - from[axis]) * t;
                assertTrue(Math.abs(arc[i][axis] - straight) <= jitter + EPS);
            }
        }
        assertEquals(1.0 / segments - jitter, arc[1][0], EPS);
    }

    @Test
    void arcWithoutRandomsIsStraight() {
        double[] from = {0.0, 0.0, 0.0};
        double[] to = {2.0, 0.0, 0.0};
        double[][] arc = BallBreakerAura.arc(from, to, 4, 0.5, null);
        assertArrayEquals(new double[] {1.0, 0.0, 0.0}, arc[2], EPS);
        assertEquals(2, BallBreakerAura.arc(from, to, 0, 0.5, null).length);
    }
}
