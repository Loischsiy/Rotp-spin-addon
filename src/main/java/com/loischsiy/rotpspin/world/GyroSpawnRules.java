package com.loischsiy.rotpspin.world;

import java.util.List;
import java.util.Locale;

/** Pure decisions behind Gyro's spawns (see {@link GyroSpawns}). No World access. */
public final class GyroSpawnRules {

    private GyroSpawnRules() {}

    /** True if the biome category name is listed (case-insensitive, surrounding spaces ignored). */
    public static boolean categoryListed(String category, List<? extends String> listed) {
        if (category == null || listed == null) {
            return false;
        }
        String wanted = category.trim().toUpperCase(Locale.ROOT);
        for (String entry : listed) {
            if (entry != null && entry.trim().toUpperCase(Locale.ROOT).equals(wanted)) {
                return true;
            }
        }
        return false;
    }

    /** The village check runs once per interval of game time. */
    public static boolean isCheckTick(long gameTime, int intervalTicks) {
        return intervalTicks > 0 && gameTime % intervalTicks == 0;
    }

    /** A roll in [0, 1) succeeds below the chance; chance 0 never succeeds, chance 1 always does. */
    public static boolean rolled(double roll, double chance) {
        return roll < chance;
    }

    /** Offset in [-spread, spread] picked from a roll in [0, 1). */
    public static int spreadOffset(double roll, int spread) {
        if (spread <= 0) {
            return 0;
        }
        int offset = (int) Math.floor(roll * (2 * spread + 1)) - spread;
        return Math.max(-spread, Math.min(spread, offset));
    }

    /**
     * Signed X offset for the horse that keeps her in Gyro's chunk: during chunk generation the
     * world region only holds that one chunk, and touching a neighbour crashes the server.
     */
    public static double sameChunkOffset(double x, double offset) {
        int chunk = chunkOf(x);
        if (chunkOf(x + offset) == chunk) {
            return offset;
        }
        if (chunkOf(x - offset) == chunk) {
            return -offset;
        }
        return 0;
    }

    private static int chunkOf(double x) {
        return ((int) Math.floor(x)) >> 4;
    }
}
