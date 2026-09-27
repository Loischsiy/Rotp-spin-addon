package com.loischsiy.rotpspin.client;

import com.loischsiy.rotpspin.power.SpinLessons;

/**
 * Spin state of the local player as last reported by the server. Plain holder without
 * client-only imports, so the packet handlers can reference it safely on both sides.
 */
public class ClientSpinState {
    private static float energy;
    private static int lesson = SpinLessons.FIRST;

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
}
