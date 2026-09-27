package com.loischsiy.rotpspin.action;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.non_stand.NonStandAction;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.entity.SteelBallEntity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

/**
 * Ability (hold): "Steel Ball Control". While held, the user's own spinning ball in flight turns
 * towards the point they look at and does not start returning; costs energy every tick.
 * Releasing the key (or running out of energy) lets the ball return as usual.
 */
public class SpinBallSteer extends NonStandAction {

    public SpinBallSteer(NonStandAction.Builder builder) {
        super(builder);
    }

    @Override
    public float getHeldTickEnergyCost(INonStandPower power) {
        return SpinConfig.STEER_ENERGY_PER_TICK.get().floatValue();
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (SteelBallEntity.findSteerable(user, SpinConfig.STEER_SEARCH_RANGE.get()) == null) {
            return conditionMessage("rotp_spin.no_ball_in_flight");
        }
        return ActionConditionResult.POSITIVE;
    }

    @Override
    protected void holdTick(World world, LivingEntity user, INonStandPower power, int ticksHeld,
            ActionTarget target, boolean requirementsFulfilled) {
        if (world.isClientSide() || !requirementsFulfilled) {
            return;
        }
        SteelBallEntity ball = SteelBallEntity.findSteerable(user, SpinConfig.STEER_SEARCH_RANGE.get());
        if (ball != null) {
            Vector3d aimPoint = user.getEyePosition(1.0F).add(user.getLookAngle().scale(SpinConfig.STEER_AIM_DISTANCE.get()));
            ball.steerTowards(aimPoint);
        }
    }
}
