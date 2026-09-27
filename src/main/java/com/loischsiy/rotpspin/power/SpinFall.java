package com.loischsiy.rotpspin.power;

/**
 * Fall distance correction for Spin users. Pure math, no World access.
 * <p>
 * RotP ({@code GameplayEventHandler#onLivingFall}, LOW) gives everyone whose power has a leap
 * {@code (leapStrength + 5) * 3} blocks of free fall, about 19 blocks for the Spin leap. Spin is a
 * technique of a human body, not a superhuman one, so for a Spin user that RotP bonus is replaced by
 * the configured reduction. A Stand's own leap bonus (if the Stand leaps) is kept, and so is anything
 * other handlers changed.
 */
public final class SpinFall {
    /** RotP applies its reduction only to falls longer than this. */
    static final float ROTP_MIN_DISTANCE = 3.0F;

    private SpinFall() {}

    /** The reduction RotP applies for a given leap strength. */
    public static float rotpReduction(float leapStrength) {
        return leapStrength > 0 ? (leapStrength + 5) * 3 : 0;
    }

    /**
     * @param original      fall distance before any handler ({@code entity.fallDistance})
     * @param current       event distance after RotP and possibly other handlers
     * @param spinLeap      the Spin leap strength RotP saw (0 if the leap is not unlocked)
     * @param standLeap     the Stand leap strength RotP saw (0 if the Stand does not leap)
     * @param spinReduction configured fall reduction for Spin, in blocks
     * @return the corrected distance
     */
    public static float correct(float original, float current, float spinLeap, float standLeap, float spinReduction) {
        float rotpLeap = Math.max(spinLeap, standLeap);
        if (original <= ROTP_MIN_DISTANCE || rotpLeap <= 0) {
            return current; // RotP did nothing
        }
        float afterRotp = Math.max(original - rotpReduction(rotpLeap), 0);
        float wanted = Math.max(original - Math.max(rotpReduction(standLeap), spinReduction), 0);
        return Math.max(current + (wanted - afterRotp), 0);
    }
}
