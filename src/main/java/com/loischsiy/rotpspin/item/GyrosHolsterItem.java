package com.loischsiy.rotpspin.item;

import java.util.List;

import javax.annotation.Nullable;

import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.holster.HolsterNbt;
import com.loischsiy.rotpspin.holster.IHolsterAccess;
import com.loischsiy.rotpspin.init.InitItems;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.KeybindTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

/**
 * Gyro's belt with side holsters. Right click loads steel balls from the inventory,
 * sneak + right click unloads them. A ball is thrown straight from the holster with the
 * "holster throw" key (works while the holster is anywhere in the inventory).
 */
public class GyrosHolsterItem extends Item {
    public static final String THROW_KEY = "key.rotp_spin.holster_throw";

    public GyrosHolsterItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack holster = player.getItemInHand(hand);
        if (!world.isClientSide()) {
            boolean changed = player.isShiftKeyDown() ? unload(player, holster) : load(player, holster);
            if (changed) {
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ARMOR_EQUIP_LEATHER, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
            player.displayClientMessage(new TranslationTextComponent("item.rotp_spin.gyros_holster.balls",
                    getBallCount(holster), capacity()), true);
        }
        return ActionResult.sidedSuccess(holster, world.isClientSide());
    }

    private static boolean load(PlayerEntity player, ItemStack holster) {
        boolean changed = false;
        PlayerInventory inventory = player.inventory;
        for (int i = 0; i < inventory.getContainerSize() && hasSpace(holster); i++) {
            ItemStack stack = inventory.getItem(i);
            while (stack.getItem() instanceof SteelBallItem && !stack.isEmpty() && insertBall(holster, stack)) {
                stack.shrink(1);
                changed = true;
            }
        }
        return changed;
    }

    private static boolean unload(PlayerEntity player, ItemStack holster) {
        boolean changed = false;
        ItemStack ball;
        while (!(ball = takeBall(holster)).isEmpty()) {
            if (!player.inventory.add(ball)) {
                player.drop(ball, false);
            }
            changed = true;
        }
        return changed;
    }

    /** Server side, from the holster throw key packet. */
    public static void throwFromHolster(ServerPlayerEntity player) {
        if (!player.isAlive() || player.isSpectator()
                || player.getCooldowns().isOnCooldown(InitItems.STEEL_BALL.get())) {
            return;
        }
        ItemStack holster = IHolsterAccess.current().findHolster(player, h -> getBallCount(h) > 0);
        if (holster.isEmpty()) {
            player.displayClientMessage(new TranslationTextComponent("rotp_spin.message.holster_empty")
                    .withStyle(TextFormatting.RED), true);
            return;
        }
        ItemStack ball = takeBall(holster);
        if (!(ball.getItem() instanceof SteelBallItem)) {
            return;
        }
        if (player.abilities.instabuild) {
            // Creative: the thrown ball is not picked up again (as SteelBallItem#use), so keep it in the holster.
            insertBall(holster, ball.copy());
        }
        SteelBallItem.throwBall(player.level, player, ball, true);
    }

    public static int capacity() {
        return SpinConfig.HOLSTER_CAPACITY.get();
    }

    public static int getBallCount(ItemStack holster) {
        return HolsterNbt.count(holster.getTag());
    }

    public static boolean hasSpace(ItemStack holster) {
        return getBallCount(holster) < capacity();
    }

    /** Puts one ball from {@code ball} into the holster (does not shrink {@code ball}). */
    public static boolean insertBall(ItemStack holster, ItemStack ball) {
        if (ball.isEmpty() || !(ball.getItem() instanceof SteelBallItem)) {
            return false;
        }
        ItemStack single = ball.copy();
        single.setCount(1);
        return HolsterNbt.push(holster.getOrCreateTag(), single.save(new CompoundNBT()), capacity());
    }

    public static ItemStack takeBall(ItemStack holster) {
        CompoundNBT ballNbt = HolsterNbt.pop(holster.getTag());
        return ballNbt == null ? ItemStack.EMPTY : ItemStack.of(ballNbt);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new TranslationTextComponent("item.rotp_spin.gyros_holster.balls", getBallCount(stack), capacity())
                .withStyle(TextFormatting.GRAY));
        tooltip.add(new TranslationTextComponent("item.rotp_spin.gyros_holster.throw_hint", new KeybindTextComponent(THROW_KEY))
                .withStyle(TextFormatting.GRAY));
        tooltip.add(new TranslationTextComponent("item.rotp_spin.gyros_holster.usage").withStyle(TextFormatting.DARK_GRAY));
    }
}
