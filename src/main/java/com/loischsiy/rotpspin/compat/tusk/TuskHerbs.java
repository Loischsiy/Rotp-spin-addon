package com.loischsiy.rotpspin.compat.tusk;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.config.SpinConfig;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Tusk nail regrowth sped up by herbs (docs/spin-lore.md, Tusk: "mint and chamomile", SBR ch. 45).
 * Chewing a herb from the {@code rotp_spin:tusk_nail_herbs} tag gives a timed infusion; while it
 * lasts, every {@code intervalTicks} one extra nail grows on top of Tusk's own regeneration.
 * Tusk's own tea already refills all nails at once, so this is a slower, cheaper complement.
 * No Tusk class is referenced at compile time: the capability is read from
 * {@code TuskCapabilityProvider.CAPABILITY} and its public nail getters/setters are called via
 * reflection; any mismatch disables the feature with a single warning.
 */
public class TuskHerbs {
    public static final ITag.INamedTag<Item> HERBS =
            ItemTags.createOptional(new ResourceLocation(AddonMain.MOD_ID, "tusk_nail_herbs"));
    static final String NBT_KEY = "rotp_spin_tusk_herb_ticks";
    private static final String PROVIDER_CLASS = "com.doggys_tilt.rotp_t.capability.TuskCapabilityProvider";
    private static final String TUSK_NAMESPACE = "rotp_t";

    private Capability<?> capability;
    private Method getNailCount;
    private Method setNailCount;
    private boolean broken;

    @SubscribeEvent
    public void onChewHerb(PlayerInteractEvent.RightClickItem event) {
        PlayerEntity player = event.getPlayer();
        ItemStack stack = event.getItemStack();
        if (!SpinConfig.COMPAT_TUSK_ENABLED.get() || !SpinConfig.COMPAT_TUSK_HERBS_ENABLED.get()
                || stack.isEmpty() || !HERBS.contains(stack.getItem()) || !hasTusk(player)) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(ActionResultType.sidedSuccess(player.level.isClientSide()));
        if (player.level.isClientSide()) {
            return;
        }
        CompoundNBT data = player.getPersistentData();
        int duration = SpinConfig.COMPAT_TUSK_HERB_DURATION.get();
        data.putInt(NBT_KEY, TuskHerbRules.extendInfusion(data.getInt(NBT_KEY), duration,
                duration * SpinConfig.COMPAT_TUSK_HERB_MAX_STACK.get()));
        if (!player.abilities.instabuild) {
            stack.shrink(1);
        }
        player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GENERIC_EAT, SoundCategory.PLAYERS, 0.6F, 1.2F);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide()) {
            return;
        }
        CompoundNBT data = event.player.getPersistentData();
        int remaining = data.getInt(NBT_KEY);
        if (remaining <= 0) {
            return;
        }
        remaining--;
        if (remaining <= 0) {
            data.remove(NBT_KEY);
        }
        else {
            data.putInt(NBT_KEY, remaining);
        }
        if (!SpinConfig.COMPAT_TUSK_ENABLED.get() || !SpinConfig.COMPAT_TUSK_HERBS_ENABLED.get()) {
            return;
        }
        Object cap = tuskCap(event.player);
        if (cap == null) {
            return;
        }
        int nails = invokeGet(cap);
        if (nails >= 0 && TuskHerbRules.growsNailThisTick(remaining, SpinConfig.COMPAT_TUSK_HERB_INTERVAL.get(), nails, TuskHerbRules.MAX_NAILS)) {
            invokeSet(cap, nails + 1);
        }
    }

    static boolean hasTusk(PlayerEntity player) {
        return IStandPower.getStandPowerOptional(player).resolve()
                .map(power -> power.hasPower() && power.getType() != null
                        && power.getType().getRegistryName() != null
                        && TUSK_NAMESPACE.equals(power.getType().getRegistryName().getNamespace()))
                .orElse(false);
    }

    private Object tuskCap(PlayerEntity player) {
        if (broken) {
            return null;
        }
        try {
            if (capability == null) {
                Field field = Class.forName(PROVIDER_CLASS).getField("CAPABILITY");
                capability = (Capability<?>) field.get(null);
                if (capability == null) {
                    return null; // not registered yet
                }
            }
            Object cap = player.getCapability(capability).resolve().orElse(null);
            if (cap != null && getNailCount == null) {
                getNailCount = cap.getClass().getMethod("getNailCount");
                setNailCount = cap.getClass().getMethod("setNailCount", int.class);
            }
            return cap;
        }
        catch (ReflectiveOperationException | ClassCastException e) {
            disable(e);
            return null;
        }
    }

    private int invokeGet(Object cap) {
        try {
            return (Integer) getNailCount.invoke(cap);
        }
        catch (ReflectiveOperationException | ClassCastException e) {
            disable(e);
            return -1;
        }
    }

    private void invokeSet(Object cap, int nails) {
        try {
            setNailCount.invoke(cap, nails);
        }
        catch (ReflectiveOperationException e) {
            disable(e);
        }
    }

    private void disable(Exception e) {
        if (!broken) {
            broken = true;
            AddonMain.LOGGER.warn("[rotp_spin] rotp_t nail capability not accessible, herb nail regrowth disabled", e);
        }
    }
}
