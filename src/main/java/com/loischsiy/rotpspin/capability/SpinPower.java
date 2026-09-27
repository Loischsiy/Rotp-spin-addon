package com.loischsiy.rotpspin.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Spin energy stored on a player. Pure logic, no World access: limits are passed in
 * by the caller (from {@link com.loischsiy.rotpspin.config.SpinConfig}).
 */
public class SpinPower implements INBTSerializable<CompoundNBT> {
    private static final String ENERGY_KEY = "Energy";

    private float energy;

    // Last state sent to the client; not saved. -1 forces a sync on login / respawn (new capability instance).
    private int syncedWholeEnergy = -1;
    private float syncedMax = -1;
    private float syncedCost = -1;

    public float getEnergy() {
        return energy;
    }

    public void setEnergy(float energy, float max) {
        this.energy = clamp(energy, max);
    }

    /** Restores energy by {@code regen}, never exceeding {@code max}. */
    public void tick(float regen, float max) {
        setEnergy(energy + regen, max);
    }

    /** Consumes {@code cost} if there is enough energy. Returns whether it was consumed. */
    public boolean tryConsume(float cost) {
        if (cost <= 0) {
            return true;
        }
        if (energy < cost) {
            return false;
        }
        energy -= cost;
        return true;
    }

    /**
     * Returns true (and remembers the state as sent) if the client HUD is outdated:
     * energy changed by a whole unit, or max / throw cost changed. Keeps packets rare
     * while energy regenerates by fractions every tick.
     */
    public boolean pollSyncNeeded(float max, float cost) {
        int whole = (int) Math.floor(energy);
        if (whole == syncedWholeEnergy && max == syncedMax && cost == syncedCost) {
            return false;
        }
        syncedWholeEnergy = whole;
        syncedMax = max;
        syncedCost = cost;
        return true;
    }

    public void copyFrom(SpinPower other) {
        this.energy = other.energy;
    }

    private static float clamp(float value, float max) {
        return Math.max(0, Math.min(value, Math.max(0, max)));
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putFloat(ENERGY_KEY, energy);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        energy = Math.max(0, nbt.getFloat(ENERGY_KEY));
    }
}
