package com.loischsiy.rotpspin.entity;

/**
 * Lesson 3 "Believe in the rotation" for blocks: which blocks can be ripped out and thrown.
 * Pure rules over unpacked block properties (the caller reads them off the BlockState),
 * so they stay testable without bootstrapping vanilla registries.
 */
public final class SpinBlock {
    private SpinBlock() {}

    /**
     * @param isAir         the block is air
     * @param destroySpeed  {@code state.getDestroySpeed}; negative = unbreakable (bedrock, barriers)
     * @param fluidEmpty    the block holds no fluid
     * @param hasTile       a tile entity (chest, furnace, ...) lives there
     */
    public static boolean canSpin(boolean isAir, float destroySpeed, boolean fluidEmpty, boolean hasTile) {
        if (isAir || hasTile || !fluidEmpty) {
            return false;
        }
        return destroySpeed >= 0;
    }
}
