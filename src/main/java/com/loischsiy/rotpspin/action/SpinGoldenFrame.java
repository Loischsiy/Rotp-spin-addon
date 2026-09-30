package com.loischsiy.rotpspin.action;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.non_stand.NonStandAction;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.power.SpinData;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/**
 * Ability (hold, lesson 4): "Golden Rectangle". The user frames the golden rectangle with both hands
 * and keeps it in the eye as the calibration reference, so Golden Spin works for a while even where
 * nature shows no golden-ratio markers. Wekapipo struck Gyro's hands precisely so that he could not
 * fold the rectangle with them (docs/spin-lore.md, "Золотой Спин и среда", ch. 51-54): a hit breaks
 * the framing. The mechanic itself is a gameplay assumption.
 */
public class SpinGoldenFrame extends NonStandAction {

    public SpinGoldenFrame(NonStandAction.Builder builder) {
        super(builder);
    }

    private static int framingTicks() {
        return SpinConfig.HAND_FRAME_HOLD_TICKS.get();
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!user.getMainHandItem().isEmpty() || !user.getOffhandItem().isEmpty()) {
            return conditionMessage("rotp_spin.hands_busy");
        }
        return ActionConditionResult.POSITIVE;
    }

    /** Energy is spent only while the rectangle is being framed, not after it is set. */
    @Override
    public float getHeldTickEnergyCost(INonStandPower power) {
        return power.getHeldActionTicks() < framingTicks()
                ? SpinConfig.HAND_FRAME_ENERGY_PER_TICK.get().floatValue() : 0;
    }

    /** No cooldown if the framing was abandoned before it was complete. */
    @Override
    protected int getCooldownAdditional(INonStandPower power, int ticksHeld) {
        return power.isUserCreative() || ticksHeld < framingTicks() ? 0 : SpinConfig.HAND_FRAME_COOLDOWN_TICKS.get();
    }

    @Override
    public boolean cancelHeldOnGettingAttacked(INonStandPower power, DamageSource dmgSource, float dmgAmount) {
        return true;
    }

    @Override
    protected void holdTick(World world, LivingEntity user, INonStandPower power, int ticksHeld,
            ActionTarget target, boolean requirementsFulfilled) {
        if (world.isClientSide() || !requirementsFulfilled || ticksHeld != framingTicks()) {
            return;
        }
        int duration = SpinConfig.HAND_FRAME_DURATION_TICKS.get();
        SpinData.of(user).ifPresent(data -> data.frameGoldenRectangle(world.getGameTime(), duration));
        ((ServerWorld) world).sendParticles(ParticleTypes.END_ROD,
                user.getX(), user.getEyeY() - 0.2, user.getZ(), 8, 0.25, 0.15, 0.25, 0.01);
        if (user instanceof PlayerEntity) {
            ((PlayerEntity) user).displayClientMessage(new TranslationTextComponent(
                    "rotp_spin.message.golden_frame", duration / 20).withStyle(TextFormatting.GOLD), true);
        }
    }
}
