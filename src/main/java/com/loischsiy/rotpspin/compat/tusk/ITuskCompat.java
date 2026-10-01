package com.loischsiy.rotpspin.compat.tusk;

/**
 * Golden Spin for the optional Tusk stand addon ({@code rotp_t}, docs/integrations.md).
 * Active implementation lives in {@link TuskCompat} and is loaded only when the mod is present;
 * otherwise this NOOP is used and no Tusk class is ever touched.
 */
public interface ITuskCompat {
    boolean isActive();

    /**
     * Server: a spinning steel ball hit a living target. Returns true if it unwound Tusk's
     * infinite rotation (counter-rotation, SBR ch. 86-87). Targets are plain vanilla types.
     */
    boolean onSpinBallHit(net.minecraft.entity.LivingEntity target, net.minecraft.entity.LivingEntity thrower, boolean chipped);

    ITuskCompat NOOP = new ITuskCompat() {
        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public boolean onSpinBallHit(net.minecraft.entity.LivingEntity target, net.minecraft.entity.LivingEntity thrower, boolean chipped) {
            return false;
        }
    };
}
