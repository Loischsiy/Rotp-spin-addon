package com.loischsiy.rotpspin.client;

import com.loischsiy.rotpspin.power.SpinLessons;

/**
 * Spin state of the local player as last reported by the server. Plain holder without
 * client-only imports, so the packet handlers can reference it safely on both sides.
 */
public class ClientSpinState {
    private static float energy;
    private static int lesson = SpinLessons.FIRST;
    /** Server charge settings; 0 = not received yet (callers fall back to the local config). */
    private static int chargeMaxTicks;
    private static double chargeChippedMax;

    public static void setEnergy(float energy) {
        ClientSpinState.energy = energy;
    }

    public static float getEnergy() {
        return energy;
    }

    public static void setLesson(int lesson) {
        ClientSpinState.lesson = SpinLessons.clamp(lesson);
    }

    /** Lesson in effect; lesson 1 until the first sync arrives. */
    public static int getLesson() {
        return lesson;
    }

    public static void setChargeConfig(int maxTicks, double chippedMax) {
        chargeMaxTicks = Math.max(0, maxTicks);
        chargeChippedMax = chippedMax;
    }

    public static void clearChargeConfig() {
        chargeMaxTicks = 0;
        chargeChippedMax = 0;
    }

    public static boolean hasChargeConfig() {
        return chargeMaxTicks > 0;
    }

    /** Server value if received, otherwise {@code fallback}. */
    public static int chargeMaxTicks(int fallback) {
        return hasChargeConfig() ? chargeMaxTicks : fallback;
    }

    /** Server value if received, otherwise {@code fallback}. */
    public static double chargeChippedMax(double fallback) {
        return hasChargeConfig() ? chargeChippedMax : fallback;
    }
}
