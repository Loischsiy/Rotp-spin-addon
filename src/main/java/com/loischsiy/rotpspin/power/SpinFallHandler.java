package com.loischsiy.rotpspin.power;

import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.init.InitPowers;

import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/** Runs after RotP's fall handler (HIGHEST) and undoes its leap bonus for Spin, see {@link SpinFall}. */
@EventBusSubscriber(modid = AddonMain.MOD_ID)
public class SpinFallHandler {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntityLiving();
        INonStandPower.getNonStandPowerOptional(entity).ifPresent(power -> {
            if (power.getType() != InitPowers.SPIN.get() || !power.isLeapUnlocked()) {
                return;
            }
            float standLeap = IStandPower.getStandPowerOptional(entity)
                    .map(stand -> stand.hasPower() && stand.isLeapUnlocked() ? stand.leapStrength() : 0F)
                    .orElse(0F);
            // entity.fallDistance is reset only after causeFallDamage, so it still holds the original distance.
            float original = Math.max(entity.fallDistance, event.getDistance());
            event.setDistance(SpinFall.correct(original, event.getDistance(), power.leapStrength(), standLeap,
                    SpinConfig.LEAP_FALL_REDUCTION.get().floatValue()));
        });
    }
}
