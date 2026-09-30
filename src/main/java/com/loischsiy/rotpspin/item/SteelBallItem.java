package com.loischsiy.rotpspin.item;

import java.util.List;

import javax.annotation.Nullable;

import com.loischsiy.rotpspin.capability.SpinPower;
import com.loischsiy.rotpspin.capability.SpinPowerCapability;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.entity.SteelBallEntity;
import com.loischsiy.rotpspin.power.SpinPowerType;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class SteelBallItem extends Item {
    private static final String DAMAGED_KEY = "Damaged";

    public SteelBallItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide()) {
            ItemStack thrown = stack.copy();
            thrown.setCount(1);
            throwBall(world, player, thrown);
        }
        if (!player.abilities.instabuild) {
            stack.shrink(1);
        }
        return ActionResult.sidedSuccess(stack, world.isClientSide());
    }

    /**
     * Server side. Launches {@code ballStack} (a single ball) from the player, with Spin if the player
     * has the Spin power and enough energy. Shared by the right click and the holster throw key; the caller removes the ball
     * from where it was taken. The cooldown is synced to the client by the server cooldown tracker.
     * A ball thrown straight from the holster ({@code fromHolster}) returns to the holster, not to the hand.
     */
    public static void throwBall(World world, PlayerEntity player, ItemStack ballStack) {
        throwBall(world, player, ballStack, false);
    }

    public static void throwBall(World world, PlayerEntity player, ItemStack ballStack, boolean fromHolster) {
        float cost = SpinConfig.BALL_SPIN_COST.get().floatValue();
        boolean creative = player.abilities.instabuild;
        // Without the Spin power it is an ordinary throw: visual rotation only, no special effects, no return.
        SpinPower spin = SpinPowerType.hasSpin(player) ? SpinPowerCapability.get(player).orElse(null) : null;
        boolean spinning = spin != null && (creative || spin.tryConsume(cost));

        SteelBallEntity ball = new SteelBallEntity(world, player, ballStack, spinning);
        ball.setFromHolster(fromHolster);
        float velocity = (spinning ? SpinConfig.BALL_SPIN_VELOCITY.get() : SpinConfig.BALL_PLAIN_VELOCITY.get()).floatValue();
        ball.shootFromRotation(player, velocity, SpinConfig.BALL_INACCURACY.get().floatValue());
        if (creative) {
            // The ball is not consumed in creative, so the returning ball must not add a copy (as vanilla TridentItem).
            ball.pickup = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
        }
        world.addFreshEntity(ball);

        // Energy level is shown by RotP's energy bar; only explain why the ball was thrown without rotation.
        if (spin != null && !spinning) {
            player.displayClientMessage(new TranslationTextComponent("rotp_spin.message.not_enough_spin",
                    (int) spin.getEnergy(), (int) cost).withStyle(TextFormatting.RED), true);
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TRIDENT_THROW, SoundCategory.PLAYERS, 1.0F, 1.2F);
        player.getCooldowns().addCooldown(ballStack.getItem(), SpinConfig.BALL_COOLDOWN_TICKS.get());
        player.awardStat(Stats.ITEM_USED.get(ballStack.getItem()));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        if (isChipped(stack)) {
            tooltip.add(new TranslationTextComponent("item.rotp_spin.steel_ball.damaged").withStyle(TextFormatting.RED));
            Item material = SteelBallRepair.repairMaterial();
            if (material != null) {
                tooltip.add(new TranslationTextComponent("item.rotp_spin.steel_ball.repair_hint", material.getDescription())
                        .withStyle(TextFormatting.DARK_GRAY));
            }
        }
        else {
            tooltip.add(new TranslationTextComponent("item.rotp_spin.steel_ball.desc").withStyle(TextFormatting.GRAY));
        }
    }

    public static boolean isChipped(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(DAMAGED_KEY);
    }

    public static void setChipped(ItemStack stack, boolean chipped) {
        if (chipped) {
            stack.getOrCreateTag().putBoolean(DAMAGED_KEY, true);
        }
        else if (stack.hasTag()) {
            stack.getTag().remove(DAMAGED_KEY);
        }
    }
}
