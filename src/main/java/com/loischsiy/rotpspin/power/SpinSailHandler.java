package com.loischsiy.rotpspin.power;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.capability.SpinPowerCapability;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.item.SteelBallItem;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * Cloak as a sail (SBR ch. 11), server half: a Spin user falling with a steel ball in hand and a
 * sail (item tag {@code rotp_spin:spin_sails}, chest slot or a Curios slot) pays Spin energy per tick
 * and takes no fall damage. The motion itself is client-side, see {@code client.SpinSailClient}.
 */
@EventBusSubscriber(modid = AddonMain.MOD_ID)
public class SpinSailHandler {
    public static final ITag.INamedTag<Item> SAILS =
            ItemTags.createOptional(new ResourceLocation(AddonMain.MOD_ID, "spin_sails"));
    private static final String SAILING_KEY = "rotp_spin_sailing";
    private static final Predicate<ItemStack> IS_SAIL = stack -> !stack.isEmpty() && SAILS.contains(stack.getItem());

    // Set by compat.curios when Curios is loaded: a cloak worn in any Curios slot counts too.
    private static BiPredicate<PlayerEntity, Predicate<ItemStack>> curioFinder = (player, filter) -> false;

    public static void setCurioFinder(BiPredicate<PlayerEntity, Predicate<ItemStack>> finder) {
        curioFinder = finder;
    }

    /** Both sides: everything but the energy and the fall itself allows the sail. */
    public static boolean canSail(PlayerEntity player) {
        if (!SpinConfig.SAIL_ENABLED.get() || player.isOnGround() || player.isFallFlying()
                || player.abilities.flying || player.isInWater() || player.isInLava() || player.isPassenger()
                || player.isSpectator()) {
            return false;
        }
        if (!(player.getMainHandItem().getItem() instanceof SteelBallItem)
                && !(player.getOffhandItem().getItem() instanceof SteelBallItem)) {
            return false;
        }
        return SpinPowerType.hasSpin(player) && hasSail(player);
    }

    private static boolean hasSail(PlayerEntity player) {
        return IS_SAIL.test(player.getItemBySlot(EquipmentSlotType.CHEST))
                || (SpinConfig.COMPAT_CURIOS_ENABLED.get() && curioFinder.test(player, IS_SAIL));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        PlayerEntity player = event.player;
        if (event.phase != TickEvent.Phase.END || player.level.isClientSide()) {
            return;
        }
        boolean sailing = player.getPersistentData().getBoolean(SAILING_KEY);
        boolean sail = canSail(player)
                && SpinSail.shouldStart(sailing, player.fallDistance, SpinConfig.SAIL_MIN_FALL_DISTANCE.get())
                && (player.abilities.instabuild || SpinPowerCapability.get(player)
                        .map(spin -> SpinSail.hasEnergy(sailing, spin.getEnergy(), SpinConfig.SAIL_COST_PER_TICK.get(),
                                SpinConfig.SAIL_START_ENERGY.get())
                                && spin.tryConsume(SpinConfig.SAIL_COST_PER_TICK.get().floatValue()))
                        .orElse(false));
        if (sail) {
            // The capped descent never builds up a dangerous fall.
            player.fallDistance = 0.0F;
        }
        if (sail != sailing) {
            player.getPersistentData().putBoolean(SAILING_KEY, sail);
        }
    }
}
