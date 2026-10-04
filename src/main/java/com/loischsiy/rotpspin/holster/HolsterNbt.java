package com.loischsiy.rotpspin.holster;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

/**
 * Pure NBT logic of the holster contents, no World or registries: each ball is stored as a full
 * saved ItemStack, so per-ball state (e.g. "damaged") survives holstering. Last in, first out.
 */
public final class HolsterNbt {
    static final String BALLS_KEY = "Balls";

    private HolsterNbt() {}

    public static int count(@Nullable CompoundNBT holsterTag) {
        return holsterTag == null ? 0 : holsterTag.getList(BALLS_KEY, Constants.NBT.TAG_COMPOUND).size();
    }

    /** Item id of the ball in slot {@code index} (0 = first in), or "" if the slot is empty. */
    public static String ballId(@Nullable CompoundNBT holsterTag, int index) {
        if (holsterTag == null || index < 0) {
            return "";
        }
        ListNBT balls = holsterTag.getList(BALLS_KEY, Constants.NBT.TAG_COMPOUND);
        return index < balls.size() ? balls.getCompound(index).getString("id") : "";
    }

    /** Adds a ball if there is room. Returns whether it was added. */
    public static boolean push(CompoundNBT holsterTag, CompoundNBT ballNbt, int capacity) {
        ListNBT balls = holsterTag.getList(BALLS_KEY, Constants.NBT.TAG_COMPOUND);
        if (balls.size() >= capacity) {
            return false;
        }
        balls.add(ballNbt);
        holsterTag.put(BALLS_KEY, balls);
        return true;
    }

    /** Removes and returns the last added ball, or null if the holster is empty. */
    @Nullable
    public static CompoundNBT pop(@Nullable CompoundNBT holsterTag) {
        if (holsterTag == null) {
            return null;
        }
        ListNBT balls = holsterTag.getList(BALLS_KEY, Constants.NBT.TAG_COMPOUND);
        if (balls.isEmpty()) {
            return null;
        }
        CompoundNBT ball = balls.getCompound(balls.size() - 1);
        balls.remove(balls.size() - 1);
        if (balls.isEmpty()) {
            holsterTag.remove(BALLS_KEY);
        }
        else {
            holsterTag.put(BALLS_KEY, balls);
        }
        return ball;
    }
}
