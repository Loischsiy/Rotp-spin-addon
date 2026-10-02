package com.loischsiy.rotpspin.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ClientSpinStateTest {

    @AfterEach
    void reset() {
        ClientSpinState.setEnergy(0);
        ClientSpinState.setLesson(1);
        ClientSpinState.clearChargeConfig();
    }

    @Test
    void chargeConfigFallsBackUntilServerValuesArrive() {
        assertEquals(false, ClientSpinState.hasChargeConfig());
        assertEquals(40, ClientSpinState.chargeMaxTicks(40));
        assertEquals(0.5, ClientSpinState.chargeChippedMax(0.5));
        ClientSpinState.setChargeConfig(60, 0.25);
        assertEquals(true, ClientSpinState.hasChargeConfig());
        assertEquals(60, ClientSpinState.chargeMaxTicks(40));
        assertEquals(0.25, ClientSpinState.chargeChippedMax(0.5));
    }

    @Test
    void keepsLastEnergy() {
        ClientSpinState.setEnergy(42.5F);
        assertEquals(42.5F, ClientSpinState.getEnergy());
    }

    @Test
    void lessonIsClampedToKnownLessons() {
        assertEquals(1, ClientSpinState.getLesson());
        ClientSpinState.setLesson(5);
        assertEquals(5, ClientSpinState.getLesson());
        ClientSpinState.setLesson(99);
        assertEquals(5, ClientSpinState.getLesson());
        ClientSpinState.setLesson(0);
        assertEquals(1, ClientSpinState.getLesson());
    }
}
