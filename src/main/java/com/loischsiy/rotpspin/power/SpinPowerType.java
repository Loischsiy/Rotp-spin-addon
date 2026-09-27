package com.loischsiy.rotpspin.power;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.loischsiy.rotpspin.capability.SpinPower;
import com.loischsiy.rotpspin.capability.SpinPowerCapability;
import com.loischsiy.rotpspin.client.ClientSpinState;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.init.InitPowers;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;

/**
 * Spin as a RotP non-stand power (pattern: RotP ZombiePowerType). Compatible with any Stand
 * (Johnny: Spin + Tusk), exclusive with Hamon / Vampirism like every non-stand power.
 * <p>
 * Energy: {@link SpinPowerCapability} is the single source of truth; RotP's energy value only
 * mirrors it (server: the capability, client: {@link ClientSpinState} synced by our packet),
 * so RotP's energy bar shows the Spin energy. Change it with /spinenergy, not /jojoenergy.
 * The leap outside the non-stand HUD mode is handled by client.SpinLeapInput.
 */
public class SpinPowerType extends NonStandPowerType<SpinData> {
    public static final int COLOR = 0xD4A017; // gold

    // Lesson 1 is the built-in RotP leap (below); hotbar actions are registered in InitPowers.
    public SpinPowerType(Action<INonStandPower>[] attacks, Action<INonStandPower>[] abilities,
            @Nullable Action<INonStandPower> defaultQuickAccess) {
        super(attacks, abilities, defaultQuickAccess, SpinData::new);
    }

    public static boolean hasSpin(LivingEntity entity) {
        return INonStandPower.getNonStandPowerOptional(entity)
                .map(power -> power.getType() == InitPowers.SPIN.get())
                .orElse(false);
    }

    // Spin is a learned discipline (as the energy capability, which is kept on death too).
    @Override
    public boolean keepOnDeath(INonStandPower power) {
        return true;
    }

    @Override
    public boolean isReplaceableWith(NonStandPowerType<?> newType) {
        return false;
    }

    @Override
    public float getTargetResolveMultiplier(INonStandPower power, IStandPower attackingStand) {
        return 1;
    }

    // ---- Energy: mirror of SpinPowerCapability ----

    @Override
    public float getMaxEnergy(INonStandPower power) {
        return SpinConfig.ENERGY_MAX.get().floatValue();
    }

    @Override
    public float tickEnergy(INonStandPower power) {
        LivingEntity user = power.getUser();
        if (user.level.isClientSide()) {
            // Our sync packet goes only to the owner, so this is the local player's value.
            return ClientSpinState.getEnergy();
        }
        return SpinPowerCapability.get(user).map(SpinPower::getEnergy).orElse(power.getEnergy());
    }

    @Override
    public boolean consumeEnergy(INonStandPower power, float amount) {
        if (power.isUserCreative()) {
            return true;
        }
        LivingEntity user = power.getUser();
        if (user.level.isClientSide()) {
            return power.getEnergy() >= amount;
        }
        return SpinPowerCapability.get(user).map(spin -> spin.tryConsume(amount)).orElse(false);
    }

    // ---- Lesson 1 "If there is a will, do it": Spin leap (Shift + Jump on the ground) ----

    @Override
    public boolean isLeapUnlocked(INonStandPower power) {
        return true;
    }

    @Override
    public float getLeapStrength(INonStandPower power) {
        float base = SpinConfig.LEAP_STRENGTH.get().floatValue();
        ModifiableAttributeInstance speed = power.getUser().getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return base;
        }
        return SpinLeap.strength(base, speed.getValue(), speed.getBaseValue(), SpinConfig.LEAP_IGNORE_SLOWNESS.get());
    }

    @Override
    public int getLeapCooldownPeriod() {
        return SpinConfig.LEAP_COOLDOWN_TICKS.get();
    }

    @Override
    public float getLeapEnergyCost() {
        return SpinConfig.LEAP_ENERGY_COST.get().floatValue();
    }
}
