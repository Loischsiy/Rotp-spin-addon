package com.loischsiy.rotpspin.action;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.non_stand.NonStandAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.power.SpinData;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/**
 * Lesson 2 "Use your muscles": Spin sent inward, through a touch, into another body. The
 * micro-vibrations take over the target's reflexes for one forced move (see {@link #forcedMove}),
 * then its muscles seize for a while (RotP's stun effect: mobs lose their AI, players are immobilized).
 * Hitting a Stand reaches its user. Each success is practice for lesson 3.
 */
public class SpinMuscleHijack extends NonStandAction {

    public SpinMuscleHijack(NonStandAction.Builder builder) {
        super(builder);
    }

    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ENTITY;
    }

    @Override
    public double getMaxRangeSqEntityTarget() {
        double reach = SpinConfig.HIJACK_REACH.get();
        return reach * reach;
    }

    @Override
    public float getEnergyCost(INonStandPower power, ActionTarget target) {
        return SpinConfig.HIJACK_ENERGY_COST.get().floatValue();
    }

    @Override
    protected int getCooldownAdditional(INonStandPower power, int ticksHeld) {
        return power.isUserCreative() ? 0 : SpinConfig.HIJACK_COOLDOWN_TICKS.get();
    }

    @Override
    protected ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, INonStandPower power) {
        LivingEntity body = bodyOf(target.getEntity());
        return ActionConditionResult.noMessage(body != null && body.isAlive() && !body.is(user));
    }

    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (world.isClientSide()) {
            return;
        }
        LivingEntity body = bodyOf(target.getEntity());
        if (body == null) {
            return;
        }
        // The rotation takes the reflexes first (one forced move), then the muscles seize.
        forcedMove(user, body);
        body.addEffect(new EffectInstance(ModStatusEffects.STUN.get(), SpinConfig.HIJACK_STUN_TICKS.get(), 0, false, false, true));
        ((ServerWorld) world).sendParticles(ParticleTypes.ENCHANTED_HIT,
                body.getX(), body.getY(0.5), body.getZ(), 12, body.getBbWidth() * 0.5, body.getBbHeight() * 0.3, body.getBbWidth() * 0.5, 0.1);
        world.playSound(null, body.getX(), body.getY(), body.getZ(),
                SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundCategory.PLAYERS, 1.0F, 1.4F);
        SpinData.of(user).ifPresent(SpinData::onMuscleHijack);
    }

    /**
     * Lesson 2 canon: the hijacked body acts against its will, and the forced shot misses the Spin user.
     * A player's hand opens and drops what it holds; a ranged mob shoots at the nearest other creature;
     * a melee mob strikes it. With nobody else around the reflex is spent on nothing.
     */
    private static void forcedMove(LivingEntity user, LivingEntity body) {
        if (body instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) body;
            if (SpinConfig.HIJACK_DISARM_PLAYERS.get() && !player.getMainHandItem().isEmpty()) {
                player.drop(true);
            }
            return;
        }
        if (!(body instanceof MobEntity) || !SpinConfig.HIJACK_FORCED_ACTION.get()) {
            return;
        }
        MobEntity mob = (MobEntity) body;
        LivingEntity victim = nearestOther(user, mob, SpinConfig.HIJACK_FORCED_RANGE.get());
        if (victim == null) {
            return;
        }
        mob.getLookControl().setLookAt(victim, 360.0F, 360.0F);
        if (mob instanceof IRangedAttackMob) {
            ((IRangedAttackMob) mob).performRangedAttack(victim, 1.0F);
        }
        else if (mob.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            mob.swing(Hand.MAIN_HAND);
            mob.doHurtTarget(victim);
        }
    }

    @Nullable
    private static LivingEntity nearestOther(LivingEntity user, LivingEntity body, double range) {
        List<LivingEntity> around = body.level.getEntitiesOfClass(LivingEntity.class, body.getBoundingBox().inflate(range),
                entity -> entity != body && entity != user && entity.isAlive() && !(entity instanceof StandEntity)
                        && EntityPredicates.NO_CREATIVE_OR_SPECTATOR.test(entity));
        LivingEntity nearest = null;
        double nearestDistSq = range * range;
        for (LivingEntity entity : around) {
            double distSq = entity.distanceToSqr(body);
            if (distSq <= nearestDistSq) {
                nearest = entity;
                nearestDistSq = distSq;
            }
        }
        return nearest;
    }

    // A Stand has no muscles of its own: the rotation goes into its user's body.
    private static LivingEntity bodyOf(Entity entity) {
        if (entity instanceof StandEntity) {
            return ((StandEntity) entity).getUser();
        }
        return entity instanceof LivingEntity ? (LivingEntity) entity : null;
    }
}
