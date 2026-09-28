package com.loischsiy.rotpspin.action;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.non_stand.NonStandAction;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.entity.SpinBlock;
import com.loischsiy.rotpspin.entity.SpunBlockEntity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;

/**
 * Lesson 3 "Believe in the rotation" for blocks (attack hotbar): the aimed block is ripped out
 * and thrown with rotation, like a crude steel ball. No return, no drops at the break —
 * the block itself is the projectile and drops where it lands.
 */
public class SpinBlockThrow extends NonStandAction {

    public SpinBlockThrow(NonStandAction.Builder builder) {
        super(builder);
    }

    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.BLOCK;
    }

    @Override
    public double getMaxRangeSqBlockTarget() {
        double reach = SpinConfig.BLOCK_SPIN_REACH.get();
        return reach * reach;
    }

    @Override
    public float getEnergyCost(INonStandPower power, ActionTarget target) {
        return SpinConfig.BLOCK_SPIN_ENERGY_COST.get().floatValue();
    }

    @Override
    protected int getCooldownAdditional(INonStandPower power, int ticksHeld) {
        return power.isUserCreative() ? 0 : SpinConfig.BLOCK_SPIN_COOLDOWN_TICKS.get();
    }

    @Override
    protected ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, INonStandPower power) {
        BlockPos pos = target.getBlockPos();
        if (pos == null || user.level.isClientSide()) {
            return ActionConditionResult.noMessage(pos != null);
        }
        BlockState state = user.level.getBlockState(pos);
        boolean ok = canSpin(user.level, pos, state);
        return ok ? ActionConditionResult.POSITIVE : conditionMessage("rotp_spin.no_block_to_spin");
    }

    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (world.isClientSide() || !(user instanceof PlayerEntity)) {
            return;
        }
        BlockPos pos = target.getBlockPos();
        if (pos == null) {
            return;
        }
        BlockState state = world.getBlockState(pos);
        if (!canSpin(world, pos, state)) {
            return;
        }
        // Respect claims and adventure mode: act as if the player broke the block.
        if (user instanceof ServerPlayerEntity
                && MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(world, pos, state, (PlayerEntity) user))) {
            return;
        }
        world.removeBlock(pos, false);
        SpunBlockEntity block = new SpunBlockEntity(world, user, state);
        block.shootFromRotation(user, SpinConfig.BLOCK_SPIN_VELOCITY.get().floatValue(),
                SpinConfig.BALL_INACCURACY.get().floatValue());
        world.addFreshEntity(block);
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.TRIDENT_THROW, SoundCategory.PLAYERS, 0.8F, 1.2F);
        user.swing(Hand.MAIN_HAND, true);
    }

    private static boolean canSpin(World world, BlockPos pos, BlockState state) {
        return SpinBlock.canSpin(state.isAir(), state.getDestroySpeed(world, pos),
                state.getFluidState().isEmpty(), world.getBlockEntity(pos) != null);
    }
}
