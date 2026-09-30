package com.loischsiy.rotpspin.power;

import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;

/**
 * Ball Breaker as the visualization of Spin energy (docs/spin-lore.md, "Ball Breaker"):
 * a summoned Ball Breaker amplifies the user's own Spin throws, but only while the throw
 * itself is Golden (lesson 4+ calibrated or lesson 5 / gallop) — in the manga it manifested
 * once, on a Golden Spin horseback throw (SBR ch. 83). Pure math stays World-free for JUnit.
 */
public final class BallBreakerBoost {
    private static final ResourceLocation BALL_BREAKER_ID = new ResourceLocation("rotp_spin", "ball_breaker");

    private BallBreakerBoost() {}

    /** Server/Client: true when the user's summoned Stand is our Ball Breaker. */
    public static boolean hasBallBreakerOut(LivingEntity user) {
        if (user == null) {
            return false;
        }
        return IStandPower.getStandPowerOptional(user).resolve()
                .map(power -> power.hasPower() && power.isActive() && isBallBreaker(power))
                .orElse(false);
    }

    private static boolean isBallBreaker(IStandPower power) {
        return power.getType() != null && BALL_BREAKER_ID.equals(power.getType().getRegistryName());
    }

    /** Pure rule: the boost applies only on top of an active Golden Spin (multiplier above x1). */
    public static boolean shouldBoost(boolean hasBreaker, double goldenMultiplier) {
        return hasBreaker && goldenMultiplier > 1.0;
    }

    /** Pure math: Golden multiplier amplified by the summoned Ball Breaker. */
    public static double boostedMultiplier(double goldenMultiplier, boolean hasBreaker, double breakerMult) {
        return shouldBoost(hasBreaker, goldenMultiplier) ? goldenMultiplier * breakerMult : goldenMultiplier;
    }
}
