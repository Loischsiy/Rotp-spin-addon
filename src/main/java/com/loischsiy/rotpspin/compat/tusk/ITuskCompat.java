package com.loischsiy.rotpspin.compat.tusk;

/**
 * Golden Spin for the optional Tusk stand addon ({@code rotp_t}, docs/integrations.md).
 * Active implementation lives in {@link TuskCompat} and is loaded only when the mod is present;
 * otherwise this NOOP is used and no Tusk class is ever touched.
 */
public interface ITuskCompat {
    boolean isActive();

    ITuskCompat NOOP = new ITuskCompat() {
        @Override
        public boolean isActive() {
            return false;
        }
    };
}
