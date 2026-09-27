package com.loischsiy.rotpspin.action;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.non_stand.NonStandAction;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.loischsiy.rotpspin.holster.IHolsterAccess;
import com.loischsiy.rotpspin.init.InitItems;
import com.loischsiy.rotpspin.item.GyrosHolsterItem;
import com.loischsiy.rotpspin.item.SteelBallItem;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * Attack hotbar / quick access: throw a steel ball from the hand (main hand first) or, if both hands
 * have none, from the holster. The Spin charge and its cost are handled by {@link SteelBallItem#throwBall},
 * the same as the item's right click, so this action itself costs nothing.
 */
public class SpinBallThrow extends NonStandAction {

    public SpinBallThrow(NonStandAction.Builder builder) {
        super(builder);
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!(user instanceof PlayerEntity)) {
            return ActionConditionResult.NEGATIVE;
        }
        PlayerEntity player = (PlayerEntity) user;
        if (player.getCooldowns().isOnCooldown(InitItems.STEEL_BALL.get())) {
            return ActionConditionResult.NEGATIVE;
        }
        if (handWithBall(player) == null
                && IHolsterAccess.current().findHolster(player, holster -> GyrosHolsterItem.getBallCount(holster) > 0).isEmpty()) {
            return conditionMessage("rotp_spin.no_steel_ball");
        }
        return ActionConditionResult.POSITIVE;
    }

    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (world.isClientSide() || !(user instanceof ServerPlayerEntity)) {
            return;
        }
        ServerPlayerEntity player = (ServerPlayerEntity) user;
        Hand hand = handWithBall(player);
        if (hand != null) {
            ItemStack stack = player.getItemInHand(hand);
            ItemStack thrown = stack.copy();
            thrown.setCount(1);
            if (!player.abilities.instabuild) {
                stack.shrink(1);
            }
            SteelBallItem.throwBall(world, player, thrown);
            player.swing(hand, true);
        }
        else {
            GyrosHolsterItem.throwFromHolster(player);
        }
    }

    @Nullable
    private static Hand handWithBall(PlayerEntity player) {
        if (player.getMainHandItem().getItem() instanceof SteelBallItem) {
            return Hand.MAIN_HAND;
        }
        if (player.getOffhandItem().getItem() instanceof SteelBallItem) {
            return Hand.OFF_HAND;
        }
        return null;
    }
}
