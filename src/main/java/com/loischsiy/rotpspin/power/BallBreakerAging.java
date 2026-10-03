package com.loischsiy.rotpspin.power;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.init.InitEffects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;

/**
 * Senescence from Ball Breaker (SBR ch. 83-84): the victim ages within seconds, and so does its Stand.
 * Each new touch deepens the aging (up to maxStacks); a Stand user also loses Stand stamina,
 * and a touch on a Stand's figure ages the user behind it. A chipped ball scales everything down.
 */
public final class BallBreakerAging {

    private BallBreakerAging() {
    }

    /** Amplifier after one more touch: -1 means "not aging yet". Never exceeds maxStacks - 1. */
    public static int nextAmplifier(int currentAmplifier, int maxStacks) {
        int cap = Math.max(1, maxStacks) - 1;
        return Math.min(Math.max(-1, currentAmplifier) + 1, cap);
    }

    /** Stand stamina the victim loses: the deeper the aging, the more the Stand withers. */
    public static float standStaminaDrain(double base, int stacks, double scale) {
        return (float) Math.max(0.0, base * Math.max(0, stacks) * Math.max(0.0, scale));
    }

    /** Attribute change of an aged body: a penalty, so always zero or negative. */
    public static double attributePenalty(double perStack, int stacks) {
        return -Math.abs(perStack) * Math.max(0, stacks);
    }

    /** Who actually ages: a Stand's figure passes the aging to its user (canon: D4C aged with Valentine). */
    @Nullable
    public static LivingEntity agingTarget(LivingEntity victim) {
        if (victim instanceof StandEntity) {
            return ((StandEntity) victim).getUser();
        }
        return victim;
    }

    /**
     * Server side: deepen the senescence of the victim (or the user behind a Stand) and wither its Stand.
     * Returns the entity that aged, or null if there was none.
     */
    @Nullable
    public static LivingEntity apply(LivingEntity victim, double scale) {
        LivingEntity target = agingTarget(victim);
        if (target == null || !target.isAlive() || target.level.isClientSide()) {
            return null;
        }
        int duration = Math.max(1, (int) Math.round(SpinConfig.BALL_BREAKER_SENESCENCE_DURATION.get() * scale));
        EffectInstance current = target.getEffect(InitEffects.SENESCENCE.get());
        int amplifier = nextAmplifier(current == null ? -1 : current.getAmplifier(),
                SpinConfig.BALL_BREAKER_MAX_STACKS.get());
        target.addEffect(new EffectInstance(InitEffects.SENESCENCE.get(), duration, amplifier));
        float drain = standStaminaDrain(SpinConfig.BALL_BREAKER_STAND_STAMINA_DRAIN.get(), amplifier + 1, scale);
        if (drain > 0) {
            IStandPower.getStandPowerOptional(target).ifPresent(power -> {
                if (power.hasPower() && power.usesStamina() && !power.isStaminaInfinite()) {
                    power.setStamina(Math.max(0, power.getStamina() - drain));
                }
            });
        }
        return target;
    }
}
