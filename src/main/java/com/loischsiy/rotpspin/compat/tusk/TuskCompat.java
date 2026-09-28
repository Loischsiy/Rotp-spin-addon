package com.loischsiy.rotpspin.compat.tusk;

import java.lang.reflect.Field;

import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.power.SpinData;
import com.loischsiy.rotpspin.power.SpinPowerType;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Golden Spin for Tusk (lesson 4 "Spin the bullets in the golden ratio"): nails shot by a Spin
 * user with a calibrated Golden Spin fly charged ({@code spinCharge} bonus damage, and Tusk's own
 * wormhole on break for charged nails). No Tusk class is referenced at compile time: the nail is
 * found by registry name and its public {@code spinCharge} field is set via reflection, so this
 * works with any {@code rotp_t} version that keeps the field and never crashes without the mod.
 */
public class TuskCompat implements ITuskCompat {
    private static final ResourceLocation NAIL_ID = new ResourceLocation("rotp_t", "nail");

    private Field spinChargeField;
    private boolean fieldMissingLogged;

    public TuskCompat() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public boolean isActive() {
        return true;
    }

    /** Spin charge bonus for the lesson, 0 without Golden Spin. Pure mapping for tests. */
    public static int chargeForLesson(int lesson, int charge4, int charge5) {
        if (lesson >= 5) {
            return charge5;
        }
        if (lesson == 4) {
            return charge4;
        }
        return 0;
    }

    @SubscribeEvent
    public void onNailSpawn(EntityJoinWorldEvent event) {
        if (event.getWorld().isClientSide() || !SpinConfig.COMPAT_TUSK_ENABLED.get()) {
            return;
        }
        Entity entity = event.getEntity();
        if (!NAIL_ID.equals(entity.getType().getRegistryName()) || !(entity instanceof ProjectileEntity)) {
            return;
        }
        Entity owner = ((ProjectileEntity) entity).getOwner();
        if (!(owner instanceof LivingEntity) || !SpinPowerType.hasSpin((LivingEntity) owner)) {
            return;
        }
        LivingEntity shooter = (LivingEntity) owner;
        int lesson = SpinData.lessonOf(shooter);
        if (lesson < 4 || SpinData.goldenMultiplier(event.getWorld(), shooter) <= 1.0) {
            return;
        }
        int charge = chargeForLesson(lesson,
                SpinConfig.COMPAT_TUSK_CHARGE_4.get(), SpinConfig.COMPAT_TUSK_CHARGE_5.get());
        if (charge > 0) {
            addCharge(entity, charge);
        }
    }

    private void addCharge(Entity nail, int charge) {
        try {
            if (spinChargeField == null) {
                spinChargeField = nail.getClass().getField("spinCharge");
            }
            spinChargeField.setInt(nail, spinChargeField.getInt(nail) + charge);
        }
        catch (ReflectiveOperationException e) {
            if (!fieldMissingLogged) {
                fieldMissingLogged = true;
                AddonMain.LOGGER.warn("[rotp_spin] rotp_t NailEntity has no spinCharge field, Golden Spin charging disabled", e);
            }
        }
    }
}
