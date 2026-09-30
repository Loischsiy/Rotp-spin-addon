package com.loischsiy.rotpspin.power;

/**
 * Spin on one's own body (docs/spin-lore.md, "Общие свойства", ch. 22, 25, 54): the body becomes
 * rigid for a moment and passes the energy of a blow on. Gyro withstood a bullet and a bomb blast,
 * Wekapipo redirected the energy of a ball. It is temporary toughness, not invulnerability, so only
 * part of a physical blow is absorbed and every absorbed point costs Spin energy. Pure math, no World access.
 */
public final class SpinBrace {
    /** Never absorb more than this share of a blow: the body stays mortal whatever the config says. */
    public static final float MAX_REDUCTION = 0.95F;

    private SpinBrace() {}

    /** The body is rigid only after the rotation has been set up. */
    public static boolean isBraced(int heldTicks, int windupTicks) {
        return heldTicks >= windupTicks;
    }

    /**
     * Only physical kinetic blows can be passed on: projectiles, explosions and direct melee hits.
     * Fire, magic-like effects and damage that bypasses armor (starvation, void, poison...) are not.
     */
    public static boolean isBraceable(boolean projectile, boolean explosion, boolean melee,
            boolean bypassArmor, boolean fire, boolean magic) {
        if (bypassArmor || fire || magic) {
            return false;
        }
        return projectile || explosion || melee;
    }

    /**
     * @param amount          incoming damage
     * @param reduction       configured share of the blow to absorb (capped by {@link #MAX_REDUCTION})
     * @param energy          Spin energy available
     * @param energyPerDamage energy spent per absorbed damage point (0 = free)
     * @return damage absorbed, limited by the energy available
     */
    public static float absorbed(float amount, float reduction, float energy, float energyPerDamage) {
        if (amount <= 0 || reduction <= 0) {
            return 0;
        }
        float wanted = amount * Math.min(reduction, MAX_REDUCTION);
        if (energyPerDamage <= 0) {
            return wanted;
        }
        return Math.max(0, Math.min(wanted, energy / energyPerDamage));
    }

    /**
     * Energy to pay for an absorbed amount. {@link #absorbed} already limits the amount by the energy,
     * so the cost is clamped to the energy left: float rounding at the boundary must not make the
     * payment fail and the whole blow go through.
     */
    public static float energyCost(float absorbed, float energyPerDamage, float energy) {
        if (absorbed <= 0 || energyPerDamage <= 0) {
            return 0;
        }
        return Math.max(0, Math.min(absorbed * energyPerDamage, energy));
    }
}
