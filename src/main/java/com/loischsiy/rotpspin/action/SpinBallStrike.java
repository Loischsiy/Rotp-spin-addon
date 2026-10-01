package com.loischsiy.rotpspin.action;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.non_stand.NonStandAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.item.SteelBallItem;
import com.loischsiy.rotpspin.power.SpinStrike;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/**
 * Lesson 1: strike a target at arm's length with the spinning steel ball held in the hand. The rotation
 * is passed on by the touch and its energy throws the body back (math in {@link SpinStrike}).
 * The ball stays in the hand; a chipped ball strikes weaker. Hitting a Stand throws its user back.
 * Not practice for lesson 2: only thrown balls count, so the lesson cannot be passed point-blank.
 */
public class SpinBallStrike extends NonStandAction {

    public SpinBallStrike(NonStandAction.Builder builder) {
        super(builder);
    }

    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ENTITY;
    }

    @Override
    public double getMaxRangeSqEntityTarget() {
        double reach = SpinConfig.STRIKE_REACH.get();
        return reach * reach;
    }

    @Override
    public float getEnergyCost(INonStandPower power, ActionTarget target) {
        return SpinConfig.STRIKE_ENERGY_COST.get().floatValue();
    }

    @Override
    protected int getCooldownAdditional(INonStandPower power, int ticksHeld) {
        return power.isUserCreative() ? 0 : SpinConfig.STRIKE_COOLDOWN_TICKS.get();
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (handWithBall(user) == null) {
            return conditionMessage("rotp_spin.no_steel_ball_in_hand");
        }
        return ActionConditionResult.POSITIVE;
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
        Hand hand = handWithBall(user);
        if (body == null || hand == null) {
            return;
        }
        boolean chipped = SteelBallItem.isChipped(user.getItemInHand(hand));
        double chippedMultiplier = SpinConfig.STRIKE_CHIPPED_MULTIPLIER.get();

        float damage = (float) SpinStrike.scaled(SpinConfig.STRIKE_DAMAGE.get(), chipped, chippedMultiplier);
        if (damage > 0) {
            DamageSource source = user instanceof PlayerEntity
                    ? DamageSource.playerAttack((PlayerEntity) user) : DamageSource.mobAttack(user);
            body.hurt(source, damage);
        }

        // The energy of the rotation goes into the touched body and throws it away from the user.
        double knockback = SpinStrike.scaled(SpinConfig.STRIKE_KNOCKBACK.get(), chipped, chippedMultiplier);
        if (knockback > 0) {
            body.knockback((float) knockback, user.getX() - body.getX(), user.getZ() - body.getZ());
            double lift = SpinStrike.scaled(SpinConfig.STRIKE_LIFT.get(), chipped, chippedMultiplier);
            Vector3d motion = body.getDeltaMovement();
            body.setDeltaMovement(motion.x, SpinStrike.liftedY(motion.y, lift), motion.z);
            body.hurtMarked = true;
        }

        user.swing(hand, true);
        ServerWorld serverWorld = (ServerWorld) world;
        serverWorld.sendParticles(ParticleTypes.CRIT,
                body.getX(), body.getY(0.5), body.getZ(), 14, body.getBbWidth() * 0.4, body.getBbHeight() * 0.3, body.getBbWidth() * 0.4, 0.3);
        serverWorld.sendParticles(ParticleTypes.ENCHANTED_HIT,
                body.getX(), body.getY(0.5), body.getZ(), 8, body.getBbWidth() * 0.3, body.getBbHeight() * 0.3, body.getBbWidth() * 0.3, 0.1);
        world.playSound(null, body.getX(), body.getY(), body.getZ(),
                SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundCategory.PLAYERS, 1.0F, chipped ? 0.8F : 1.1F);
    }

    @Nullable
    private static Hand handWithBall(LivingEntity user) {
        if (user.getMainHandItem().getItem() instanceof SteelBallItem) {
            return Hand.MAIN_HAND;
        }
        if (user.getOffhandItem().getItem() instanceof SteelBallItem) {
            return Hand.OFF_HAND;
        }
        return null;
    }

    // The strike reaches the body behind a Stand: its user is thrown back.
    @Nullable
    private static LivingEntity bodyOf(Entity entity) {
        if (entity instanceof StandEntity) {
            return ((StandEntity) entity).getUser();
        }
        return entity instanceof LivingEntity ? (LivingEntity) entity : null;
    }
}
