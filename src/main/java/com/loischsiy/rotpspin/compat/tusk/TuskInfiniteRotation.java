package com.loischsiy.rotpspin.compat.tusk;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.power.SpinData;
import com.loischsiy.rotpspin.power.SpinPowerType;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Tusk ACT4 infinite rotation through the canonical Spin route (docs/spin-lore.md, Tusk ACT4,
 * SBR ch. 85-87). A lesson 5 Spin user with Tusk on ACT4 who holds Super Spin (horse at its
 * natural gallop or the detour kick) gets Tusk's own infinite rotation charge; Tusk's own route
 * is left untouched. A Super Spin ball unwinds {@code rotp_t:infinite_rotation} on the target.
 * No Tusk class is referenced at compile time: the capability is read from
 * {@code TuskCapabilityProvider.CAPABILITY} and its public getters/setters are called via
 * reflection (the setter syncs the client itself); any mismatch disables the feature with one warning.
 */
public class TuskInfiniteRotation {
    private static final String PROVIDER_CLASS = "com.doggys_tilt.rotp_t.capability.TuskCapabilityProvider";
    private static final ResourceLocation EFFECT_ID = new ResourceLocation("rotp_t", "infinite_rotation");

    private Capability<?> capability;
    private Method getAct;
    private Method isCharged;
    private Method setCharged;
    private boolean broken;
    private Effect effect;
    private boolean effectMissingLogged;

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        PlayerEntity player = event.player;
        if (event.phase != TickEvent.Phase.END || player.level.isClientSide() || broken
                || !SpinConfig.COMPAT_TUSK_ENABLED.get() || !SpinConfig.COMPAT_TUSK_INFINITE_ENABLED.get()
                || !SpinPowerType.hasSpin(player) || !TuskHerbs.hasTusk(player)) {
            return;
        }
        int lesson = SpinData.lessonOf(player);
        int minLesson = SpinConfig.COMPAT_TUSK_INFINITE_LESSON.get();
        if (lesson < minLesson || !hasSuperSpin(player)) {
            return;
        }
        Object cap = tuskCap(player);
        if (cap == null) {
            return;
        }
        try {
            int act = (Integer) getAct.invoke(cap);
            boolean charged = (Boolean) isCharged.invoke(cap);
            if (TuskInfiniteRules.grantsCharge(true, charged, act, lesson, minLesson, true)) {
                setCharged.invoke(cap, true);
            }
        }
        catch (ReflectiveOperationException | ClassCastException | NullPointerException e) {
            disable(e);
        }
    }

    /** Server: a spinning ball hit; removes infinite rotation from the target if the ball counter-rotates it. */
    public boolean counterRotate(LivingEntity target, LivingEntity thrower, boolean chipped) {
        if (!SpinConfig.COMPAT_TUSK_ENABLED.get() || target.level.isClientSide()) {
            return false;
        }
        Effect infinite = infiniteRotationEffect();
        if (infinite == null || !target.hasEffect(infinite)) {
            return false;
        }
        if (!TuskInfiniteRules.counterRotates(SpinConfig.COMPAT_TUSK_COUNTER_ENABLED.get(), true, chipped,
                SpinData.lessonOf(thrower), SpinConfig.COMPAT_TUSK_INFINITE_LESSON.get(),
                SpinConfig.COMPAT_TUSK_COUNTER_NEEDS_SUPER_SPIN.get(), hasSuperSpin(thrower))) {
            return false;
        }
        target.removeEffect(infinite);
        if (target.getVehicle() instanceof LivingEntity) {
            ((LivingEntity) target.getVehicle()).removeEffect(infinite);
        }
        if (target.level instanceof ServerWorld) {
            ((ServerWorld) target.level).sendParticles(ParticleTypes.CRIT,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    12, target.getBbWidth() * 0.5, target.getBbHeight() * 0.3, target.getBbWidth() * 0.5, 0.2);
        }
        target.level.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.ANVIL_LAND, SoundCategory.PLAYERS, 0.4F, 1.8F);
        return true;
    }

    private static boolean hasSuperSpin(LivingEntity entity) {
        long time = entity.level.getGameTime();
        return SpinData.of(entity).map(data -> data.hasSuperSpin(time)).orElse(false);
    }

    private Effect infiniteRotationEffect() {
        if (effect == null) {
            effect = ForgeRegistries.POTIONS.getValue(EFFECT_ID);
            if (effect == null && !effectMissingLogged) {
                effectMissingLogged = true;
                AddonMain.LOGGER.warn("[rotp_spin] rotp_t has no {} effect, Spin counter-rotation disabled", EFFECT_ID);
            }
        }
        return effect;
    }

    private Object tuskCap(PlayerEntity player) {
        try {
            if (capability == null) {
                Field field = Class.forName(PROVIDER_CLASS).getField("CAPABILITY");
                capability = (Capability<?>) field.get(null);
                if (capability == null) {
                    return null; // not registered yet
                }
            }
            Object cap = player.getCapability(capability).resolve().orElse(null);
            if (cap != null && setCharged == null) {
                Class<?> type = cap.getClass();
                getAct = type.getMethod("getAct");
                isCharged = type.getMethod("isHasInfiniteRotationCharge");
                setCharged = type.getMethod("setHasInfiniteRotationCharge", boolean.class);
            }
            return cap;
        }
        catch (ReflectiveOperationException | ClassCastException e) {
            disable(e);
            return null;
        }
    }

    private void disable(Exception e) {
        if (!broken) {
            broken = true;
            AddonMain.LOGGER.warn("[rotp_spin] rotp_t infinite rotation charge not accessible, Super Spin ACT4 charging disabled", e);
        }
    }
}
