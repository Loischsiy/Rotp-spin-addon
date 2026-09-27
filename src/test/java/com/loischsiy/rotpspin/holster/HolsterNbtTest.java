package com.loischsiy.rotpspin.holster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundNBT;

class HolsterNbtTest {

    private static CompoundNBT ball(String id) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("id", id);
        return nbt;
    }

    @Test
    void emptyOrMissingTagHasNoBalls() {
        assertEquals(0, HolsterNbt.count(null));
        assertEquals(0, HolsterNbt.count(new CompoundNBT()));
        assertNull(HolsterNbt.pop(null));
        assertNull(HolsterNbt.pop(new CompoundNBT()));
    }

    @Test
    void pushRespectsCapacity() {
        CompoundNBT tag = new CompoundNBT();
        assertTrue(HolsterNbt.push(tag, ball("a"), 2));
        assertTrue(HolsterNbt.push(tag, ball("b"), 2));
        assertFalse(HolsterNbt.push(tag, ball("c"), 2));
        assertEquals(2, HolsterNbt.count(tag));
    }

    @Test
    void popIsLastInFirstOutAndKeepsBallData() {
        CompoundNBT tag = new CompoundNBT();
        CompoundNBT damaged = ball("b");
        damaged.putBoolean("Damaged", true);
        HolsterNbt.push(tag, ball("a"), 2);
        HolsterNbt.push(tag, damaged, 2);

        CompoundNBT first = HolsterNbt.pop(tag);
        assertEquals("b", first.getString("id"));
        assertTrue(first.getBoolean("Damaged"));
        assertEquals("a", HolsterNbt.pop(tag).getString("id"));
        assertNull(HolsterNbt.pop(tag));
    }

    @Test
    void emptiedHolsterDropsTheListKey() {
        CompoundNBT tag = new CompoundNBT();
        HolsterNbt.push(tag, ball("a"), 2);
        HolsterNbt.pop(tag);
        assertFalse(tag.contains(HolsterNbt.BALLS_KEY));
    }

    @Test
    void loweredCapacityBlocksPushButKeepsBalls() {
        CompoundNBT tag = new CompoundNBT();
        HolsterNbt.push(tag, ball("a"), 2);
        HolsterNbt.push(tag, ball("b"), 2);
        assertFalse(HolsterNbt.push(tag, ball("c"), 1));
        assertEquals(2, HolsterNbt.count(tag));
    }
}
