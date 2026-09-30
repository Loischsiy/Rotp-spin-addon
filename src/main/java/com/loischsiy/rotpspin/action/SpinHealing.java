package com.loischsiy.rotpspin.action;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.non_stand.NonStandAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.loischsiy.rotpspin.config.SpinConfig;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.CauldronBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/**
 * Ability (hold): "Zeppeli Medicine". The Zeppeli family are doctors; controlled rotation closes
 * wounds and drives venom out of the body. Heals the user, or with Sneak the entity under the crosshair
 * within reach (pattern: RotP HamonHealing). Interrupted by getting hit.
 * <p>
 * "X-ray": with water next to the patient (a pool or a filled cauldron), the ripples from the
 * rotation show where the ailment is (Gyro's water tray, docs/spin-lore.md, "Общие свойства"):
 * the healer sees the hidden harmful effects and the treatment is more precise (more health).
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
        BlockPos water = findWater(world, patient, SpinConfig.HEALING_XRAY_WATER_RADIUS.get());
        float amount = SpinConfig.HEALING_AMOUNT.get().floatValue();
        if (water != null) {
            amount *= SpinConfig.HEALING_XRAY_AMOUNT_MULT.get().floatValue();
            ((ServerWorld) world).sendParticles(ParticleTypes.FISHING,
                    water.getX() + 0.5, water.getY() + 0.9, water.getZ() + 0.5, 10, 0.3, 0.0, 0.3, 0.02);
            if (ticksHeld == SpinConfig.HEALING_INTERVAL_TICKS.get()) {
                diagnose(user, patient);
            }
        }
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

    /** Water (a fluid block or a filled cauldron) nearest to the patient within the radius, or null. */
    @Nullable
    static BlockPos findWater(World world, LivingEntity patient, int radius) {
        if (radius <= 0) {
            return null;
        }
        BlockPos center = patient.blockPosition();
        BlockPos nearest = null;
        double best = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -1, -radius), center.offset(radius, 1, radius))) {
            BlockState state = world.getBlockState(pos);
            boolean water = world.getFluidState(pos).is(FluidTags.WATER)
                    || state.getBlock() instanceof CauldronBlock && state.getValue(CauldronBlock.LEVEL) > 0;
            if (water) {
                double dist = pos.distSqr(center);
                if (dist < best) {
                    best = dist;
                    nearest = pos.immutable();
                }
            }
        }
        return nearest;
    }

    /** The ripples reveal the patient's harmful effects (or that nothing is hidden) to the healer. */
    private static void diagnose(LivingEntity user, LivingEntity patient) {
        if (!(user instanceof PlayerEntity)) {
            return;
        }
        IFormattableTextComponent ailments = new StringTextComponent("");
        boolean found = false;
        for (EffectInstance effect : patient.getActiveEffects()) {
            if (effect.getEffect().getCategory() == EffectType.HARMFUL) {
                if (found) {
                    ailments.append(", ");
                }
                ailments.append(effect.getEffect().getDisplayName());
                found = true;
            }
        }
        String hp = String.format("%.1f/%.1f", patient.getHealth(), patient.getMaxHealth());
        ((PlayerEntity) user).displayClientMessage(found
                ? new TranslationTextComponent("rotp_spin.message.xray_diagnosis", patient.getDisplayName(), ailments, hp)
                        .withStyle(TextFormatting.AQUA)
                : new TranslationTextComponent("rotp_spin.message.xray_healthy", patient.getDisplayName(), hp)
                        .withStyle(TextFormatting.AQUA), false);
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
