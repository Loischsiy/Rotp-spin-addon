package com.loischsiy.rotpspin.capability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SpinPowerTest {
    private static final float MAX = 100F;

    @Test
    void startsEmpty() {
        assertEquals(0F, new SpinPower().getEnergy());
    }

    @Test
    void tickRegeneratesUpToMax() {
        SpinPower spin = new SpinPower();
        spin.tick(30F, MAX);
        assertEquals(30F, spin.getEnergy());
        spin.tick(100F, MAX);
        assertEquals(MAX, spin.getEnergy());
    }

    @Test
    void tickClampsWhenMaxWasLowered() {
        SpinPower spin = new SpinPower();
        spin.setEnergy(80F, MAX);
        spin.tick(0F, 50F);
        assertEquals(50F, spin.getEnergy());
    }

    @Test
    void consumeSucceedsOnlyWithEnoughEnergy() {
        SpinPower spin = new SpinPower();
        spin.setEnergy(25F, MAX);
        assertTrue(spin.tryConsume(20F));
        assertEquals(5F, spin.getEnergy());
        assertFalse(spin.tryConsume(20F));
        assertEquals(5F, spin.getEnergy());
    }

    @Test
    void zeroCostIsAlwaysFree() {
        SpinPower spin = new SpinPower();
        assertTrue(spin.tryConsume(0F));
        assertEquals(0F, spin.getEnergy());
    }

    @Test
    void setEnergyNeverNegative() {
        SpinPower spin = new SpinPower();
        spin.setEnergy(-10F, MAX);
        assertEquals(0F, spin.getEnergy());
    }

    @Test
    void copyFromTransfersEnergy() {
        SpinPower old = new SpinPower();
        old.setEnergy(42F, MAX);
        SpinPower fresh = new SpinPower();
        fresh.copyFrom(old);
        assertEquals(42F, fresh.getEnergy());
    }

    @Test
    void freshInstanceAlwaysNeedsFirstSync() {
        SpinPower spin = new SpinPower();
        assertTrue(spin.pollSyncNeeded(MAX, 20F));
        assertFalse(spin.pollSyncNeeded(MAX, 20F));
    }

    @Test
    void fractionalRegenDoesNotSyncUntilWholeUnit() {
        SpinPower spin = new SpinPower();
        spin.pollSyncNeeded(MAX, 20F);
        spin.tick(0.25F, MAX);
        spin.tick(0.25F, MAX);
        spin.tick(0.25F, MAX);
        assertFalse(spin.pollSyncNeeded(MAX, 20F));
        spin.tick(0.25F, MAX);
        assertTrue(spin.pollSyncNeeded(MAX, 20F));
    }

    @Test
    void consumeTriggersSync() {
        SpinPower spin = new SpinPower();
        spin.setEnergy(50F, MAX);
        spin.pollSyncNeeded(MAX, 20F);
        spin.tryConsume(20F);
        assertTrue(spin.pollSyncNeeded(MAX, 20F));
    }

    @Test
    void configChangeTriggersSync() {
        SpinPower spin = new SpinPower();
        spin.setEnergy(MAX, MAX);
        spin.pollSyncNeeded(MAX, 20F);
        assertTrue(spin.pollSyncNeeded(MAX, 30F));
        assertTrue(spin.pollSyncNeeded(200F, 30F));
        assertFalse(spin.pollSyncNeeded(200F, 30F));
    }

    @Test
    void syncStateIsNotCopiedOnClone() {
        SpinPower old = new SpinPower();
        old.setEnergy(42F, MAX);
        old.pollSyncNeeded(MAX, 20F);
        SpinPower fresh = new SpinPower();
        fresh.copyFrom(old);
        assertTrue(fresh.pollSyncNeeded(MAX, 20F));
    }
}
