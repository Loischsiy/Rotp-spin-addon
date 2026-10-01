package com.loischsiy.rotpspin.compat.tusk;

/** Pure, Minecraft-free rules of the herbal nail infusion ({@link TuskHerbs}) for JUnit. */
public final class TuskHerbRules {
    /** Tusk's own nail cap (TuskCapability). */
    public static final int MAX_NAILS = 10;

    private TuskHerbRules() {}

    /** Pure rule: does an extra nail grow on this tick of the infusion (counted down to 0)? */
    public static boolean growsNailThisTick(int remainingTicks, int intervalTicks, int nails, int maxNails) {
        if (remainingTicks <= 0 || intervalTicks <= 0 || nails >= maxNails) {
            return false;
        }
        return remainingTicks % intervalTicks == 0;
    }

    /** Pure rule: a new herb extends the infusion up to the cap, never shortens it. */
    public static int extendInfusion(int remainingTicks, int durationTicks, int maxTicks) {
        return Math.max(remainingTicks, Math.min(maxTicks, Math.max(0, remainingTicks) + durationTicks));
    }
}
