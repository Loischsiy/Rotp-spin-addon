package com.loischsiy.rotpspin.action;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.non_stand.NonStandAction;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.entity.SpunItemEntity;
import com.loischsiy.rotpspin.item.SteelBallItem;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;

/**
 * Lesson 3 "Believe in the rotation" (attack hotbar): Spin carried by inanimate matter. Throws one
 * item from the main hand with rotation, as Johnny first spun a cork and then his nails.
 * Steel balls have their own throw.
 */
public class SpinItemThrow extends NonStandAction {

    public SpinItemThrow(NonStandAction.Builder builder) {
        super(builder);
    }

    @Override
    public float getEnergyCost(INonStandPower power, ActionTarget target) {
        return SpinConfig.ITEM_SPIN_ENERGY_COST.get().floatValue();
    }

    @Override
    protected int getCooldownAdditional(INonStandPower power, int ticksHeld) {
        return power.isUserCreative() ? 0 : SpinConfig.ITEM_SPIN_COOLDOWN_TICKS.get();
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!(user instanceof PlayerEntity)) {
            return ActionConditionResult.NEGATIVE;
        }
        ItemStack held = user.getMainHandItem();
        if (held.isEmpty() || held.getItem() instanceof SteelBallItem) {
            return conditionMessage("rotp_spin.no_item_to_spin");
        }
        return ActionConditionResult.POSITIVE;
    }

    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (world.isClientSide() || !(user instanceof PlayerEntity)) {
            return;
        }
        PlayerEntity player = (PlayerEntity) user;
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            return;
        }
        ItemStack thrown = held.copy();
        thrown.setCount(1);
        if (!player.abilities.instabuild) {
            held.shrink(1);
        }
        SpunItemEntity entity = new SpunItemEntity(world, player, thrown);
        entity.shootFromRotation(player, SpinConfig.ITEM_SPIN_VELOCITY.get().floatValue(),
                SpinConfig.BALL_INACCURACY.get().floatValue());
        world.addFreshEntity(entity);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TRIDENT_THROW, SoundCategory.PLAYERS, 0.8F, 1.5F);
        player.swing(Hand.MAIN_HAND, true);
    }
}
