package com.loischsiy.rotpspin.action;

import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.capability.SpinPower;
import com.loischsiy.rotpspin.capability.SpinPowerCapability;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.entity.SpunBlockEntity;
import com.loischsiy.rotpspin.power.SpinData;
import com.loischsiy.rotpspin.power.SpinPowerType;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * Lesson 3 "Believe in the rotation" for a held block: sneak + right click with a block in the
 * main hand throws it with Spin, like a steel ball, but the crude matter is spent on the first
 * hit (no return). Only with the Spin power. Plain right click still places the block.
 */
@EventBusSubscriber(modid = AddonMain.MOD_ID)
public class SpinBlockHandThrow {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != Hand.MAIN_HAND || event.getWorld().isClientSide()) {
            return;
        }
        PlayerEntity player = event.getPlayer();
        if (tryThrow(player, player.getItemInHand(Hand.MAIN_HAND))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickItem event) {
        if (event.getHand() != Hand.MAIN_HAND || event.getWorld().isClientSide()) {
            return;
        }
        PlayerEntity player = event.getPlayer();
        if (tryThrow(player, player.getItemInHand(Hand.MAIN_HAND))) {
            event.setCanceled(true);
        }
    }

    private static boolean tryThrow(PlayerEntity player, ItemStack stack) {
        if (!(player instanceof ServerPlayerEntity) || !player.isShiftKeyDown()
                || !(stack.getItem() instanceof BlockItem)
                || !SpinPowerType.hasSpin(player) || !isLessonLearned(player)) {
            return false;
        }
        World world = player.level;
        SpinPower spin = SpinPowerCapability.get(player).orElse(null);
        float cost = SpinConfig.BLOCK_SPIN_ENERGY_COST.get().floatValue();
        if (spin == null || !(player.abilities.instabuild || spin.tryConsume(cost))) {
            player.displayClientMessage(new TranslationTextComponent("jojo.message.action_condition.no_energy_spin")
                    .withStyle(TextFormatting.RED), true);
            return true;
        }
        BlockState state = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
        SpunBlockEntity block = new SpunBlockEntity(world, player, state);
        block.shootFromRotation(player, SpinConfig.BLOCK_SPIN_VELOCITY.get().floatValue(),
                SpinConfig.BALL_INACCURACY.get().floatValue());
        world.addFreshEntity(block);
        if (!player.abilities.instabuild) {
            stack.shrink(1);
        }
        player.getCooldowns().addCooldown(stack.getItem(), SpinConfig.BLOCK_SPIN_COOLDOWN_TICKS.get());
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TRIDENT_THROW, SoundCategory.PLAYERS, 0.8F, 1.2F);
        player.swing(Hand.MAIN_HAND, true);
        return true;
    }

    private static boolean isLessonLearned(PlayerEntity player) {
        if (!SpinConfig.LESSONS_ENABLED.get()) {
            return true;
        }
        return SpinData.of(player).map(data -> data.getLesson() >= 3).orElse(false);
    }
}
