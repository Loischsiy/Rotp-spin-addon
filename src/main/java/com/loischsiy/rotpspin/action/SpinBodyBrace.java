package com.loischsiy.rotpspin.action;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.non_stand.NonStandAction;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.power.SpinBrace;

import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/**
 * Ability (hold, lesson 1 "If there is a will, do it": Spin outward, onto one's own body).
 * The user sets his body spinning: after a short windup it is rigid and passes the energy of
 * a kinetic blow on (Gyro withstood a bullet and a bomb blast, Wekapipo redirected a ball,
 * docs/spin-lore.md "Общие свойства", ch. 22, 25, 54). The damage reduction itself lives in
 * {@link com.loischsiy.rotpspin.power.SpinBraceHandler}; the price is energy per tick, energy per
 * absorbed blow and a stiff, slow body.
 */
public class SpinBodyBrace extends NonStandAction {

    public SpinBodyBrace(NonStandAction.Builder builder) {
        super(builder);
    }

    private static int windupTicks() {
        return SpinConfig.BRACE_WINDUP_TICKS.get();
    }

    @Override
    public float getHeldTickEnergyCost(INonStandPower power) {
        return SpinConfig.BRACE_ENERGY_PER_TICK.get().floatValue();
    }

    /** No cooldown if the rotation was dropped before the body became rigid. */
    @Override
    protected int getCooldownAdditional(INonStandPower power, int ticksHeld) {
        return power.isUserCreative() || !SpinBrace.isBraced(ticksHeld, windupTicks())
                ? 0 : SpinConfig.BRACE_COOLDOWN_TICKS.get();
    }

    /** The point of the stance is to take the blow, so a hit must not interrupt it. */
    @Override
    public boolean cancelHeldOnGettingAttacked(INonStandPower power, DamageSource dmgSource, float dmgAmount) {
        return false;
    }

    @Override
    protected void holdTick(World world, LivingEntity user, INonStandPower power, int ticksHeld,
            ActionTarget target, boolean requirementsFulfilled) {
        if (world.isClientSide() || !requirementsFulfilled || !SpinBrace.isBraced(ticksHeld, windupTicks())) {
            return;
        }
        int slowness = SpinConfig.BRACE_SLOWNESS_AMPLIFIER.get();
        if (slowness >= 0) {
            user.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 5, slowness, false, false, false));
        }
        if (ticksHeld == windupTicks()) {
            ((ServerWorld) world).sendParticles(ParticleTypes.CRIT,
                    user.getX(), user.getY(0.5), user.getZ(), 10, 0.3, 0.5, 0.3, 0.05);
        }
    }
}
