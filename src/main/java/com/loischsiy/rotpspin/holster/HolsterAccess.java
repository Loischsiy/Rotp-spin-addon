package com.loischsiy.rotpspin.holster;

/**
 * Holds the {@link IHolsterAccess} chosen at startup: inventory only by default,
 * Curios "belt" slot + inventory when Curios is installed (see AddonMain#commonSetup).
 */
public final class HolsterAccess {
    private static IHolsterAccess instance = InventoryHolsterAccess.INSTANCE;

    private HolsterAccess() {}

    public static IHolsterAccess get() {
        return instance;
    }

    public static void set(IHolsterAccess access) {
        instance = access;
    }
}
