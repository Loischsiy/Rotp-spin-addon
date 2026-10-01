package com.loischsiy.rotpspin.power;

/**
 * Ball Breaker's look (docs/spin-lore.md, "Ball Breaker"): a cloud of Spin energy around the Stand
 * resembling electrostatic discharges (SBR ch. 83, JOJOVELLER). Canon only as appearance: the aura
 * deals no damage and stuns nothing. Pure math, no World access, so JUnit can check it.
 *
 * Offsets are relative to the centre of the Stand's body; random inputs come from the caller
 * (entity RNG in game, fixed values in tests).
 */
public final class BallBreakerAura {

    private BallBreakerAura() {}

    /** Sparks this tick: scaled by the Stand's summon fade-in alpha, never negative. */
    public static int sparkCount(int sparksPerTick, float alpha) {
        if (sparksPerTick <= 0 || alpha <= 0F) {
            return 0;
        }
        return Math.round(sparksPerTick * Math.min(alpha, 1F));
    }

    /** Vertical semi-axis of the cloud: the Stand's half height plus half the horizontal padding. */
    public static double verticalRadius(double bodyHeight, double radius) {
        return Math.max(bodyHeight, 0.0) * 0.5 + radius * 0.5;
    }

    /**
     * A point on the ellipsoid shell around the body.
     * @param u       0..1, longitude
     * @param v       0..1, latitude (uniform over the shell area)
     * @param radius  horizontal semi-axis
     * @param height  vertical semi-axis
     * @return {dx, dy, dz} from the body centre
     */
    public static double[] shellPoint(double u, double v, double radius, double height) {
        double phi = u * 2.0 * Math.PI;
        double cosTheta = 1.0 - 2.0 * clamp01(v);
        double sinTheta = Math.sqrt(Math.max(0.0, 1.0 - cosTheta * cosTheta));
        return new double[] {
                radius * sinTheta * Math.cos(phi),
                height * cosTheta,
                radius * sinTheta * Math.sin(phi)
        };
    }

    /**
     * A jagged discharge between two shell points: {@code segments + 1} points, exact endpoints,
     * inner joints displaced by up to {@code jitter} on each axis.
     * @param randoms 3 * (segments - 1) values in 0..1; missing values count as 0.5 (no displacement)
     */
    public static double[][] arc(double[] from, double[] to, int segments, double jitter, double[] randoms) {
        int n = Math.max(1, segments);
        double[][] points = new double[n + 1][];
        for (int i = 0; i <= n; i++) {
            double t = (double) i / n;
            double[] p = new double[3];
            for (int axis = 0; axis < 3; axis++) {
                p[axis] = from[axis] + (to[axis] - from[axis]) * t;
                if (i > 0 && i < n) {
                    int r = (i - 1) * 3 + axis;
                    double rnd = randoms != null && r < randoms.length ? clamp01(randoms[r]) : 0.5;
                    p[axis] += (rnd - 0.5) * 2.0 * jitter;
                }
            }
            points[i] = p;
        }
        return points;
    }

    private static double clamp01(double x) {
        return x < 0.0 ? 0.0 : (x > 1.0 ? 1.0 : x);
    }
}
