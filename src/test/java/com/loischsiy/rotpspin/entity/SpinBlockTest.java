package com.loischsiy.rotpspin.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SpinBlockTest {
    @Test
    void ordinaryBlockSpins() {
        assertTrue(SpinBlock.canSpin(false, 1.5F, true, false));
    }

    @Test
    void airDoesNotSpin() {
        assertFalse(SpinBlock.canSpin(true, 0.0F, true, false));
    }

    @Test
    void unbreakableDoesNotSpin() {
        assertFalse(SpinBlock.canSpin(false, -1.0F, true, false));
    }

    @Test
    void tileEntityDoesNotSpin() {
        assertFalse(SpinBlock.canSpin(false, 1.5F, true, true));
    }

    @Test
    void fluidDoesNotSpin() {
        assertFalse(SpinBlock.canSpin(false, 100.0F, false, false));
    }
}
