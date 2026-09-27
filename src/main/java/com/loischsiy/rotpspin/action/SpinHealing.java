package com.loischsiy.rotpspin.action;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.non_stand.NonStandAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.loischsiy.rotpspin.config.SpinConfig;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/**
 * Ability (hold): "Zeppeli Medicine". The Zeppeli family are doctors; controlled rotation closes
 * wounds and drives venom out of the body. Heals the user, or with Sneak the entity under the crosshair
 * within reach (pattern: RotP HamonHealing). Interrupted by getting hit.
 */
public class SpinHealing extends NonStandAction {

    public SpinHealing(NonStandAction.Builder builder) {
        super(builder);
    }

    @Override
    public float getHeldTickEnergyCost(INonStandPower power) {
        return SpinConfig.HEALING_ENERGY_PER_TICK.get().floatValue();
    }

    @Override
    public boolean cancelHeldOnGettingAttacked(INonStandPower power, DamageSource dmgSource, float dmgAmount) {
        return true;
    }

    @Override
    protected void holdTick(World world, LivingEntity user, INonStandPower power, int ticksHeld,
            ActionTarget target, boolean requirementsFulfilled) {
        if (world.isClientSide() || !requirementsFulfilled
                || ticksHeld <= 0 || ticksHeld % SpinConfig.HEALING_INTERVAL_TICKS.get() != 0) {
            return;
        }
        LivingEntity patient = choosePatient(user, target);
        float amount = SpinConfig.HEALING_AMOUNT.get().floatValue();
        if (amount > 0) {
            patient.heal(amount);
        }
        if (SpinConfig.HEALING_CURES_HARMFUL.get()) {
            patient.removeEffect(Effects.POISON);
            patient.removeEffect(Effects.WITHER);
            patient.removeEffect(ModStatusEffects.BLEEDING.get());
        }
        ((ServerWorld) world).sendParticles(ParticleTypes.HAPPY_VILLAGER,
                patient.getX(), patient.getY(0.5), patient.getZ(), 6,
                patient.getBbWidth() * 0.4, patient.getBbHeight() * 0.3, patient.getBbWidth() * 0.4, 0);
    }

    private static LivingEntity choosePatient(LivingEntity user, ActionTarget target) {
        if (!user.isShiftKeyDown() || target.getType() != TargetType.ENTITY) {
            return user;
        }
        Entity entity = target.getEntity();
        if (!(entity instanceof LivingEntity) || entity instanceof StandEntity || !entity.isAlive()) {
            return user;
        }
        double reach = SpinConfig.HEALING_TARGET_REACH.get();
        return user.distanceToSqr(entity) <= reach * reach ? (LivingEntity) entity : user;
    }
}
