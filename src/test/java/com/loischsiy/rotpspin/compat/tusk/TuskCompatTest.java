package com.loischsiy.rotpspin.compat.tusk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TuskCompatTest {
    @Test
    void chargeByLesson() {
        assertEquals(0, TuskCompat.chargeForLesson(0, 2, 4));
        assertEquals(0, TuskCompat.chargeForLesson(3, 2, 4));
        assertEquals(2, TuskCompat.chargeForLesson(4, 2, 4));
        assertEquals(4, TuskCompat.chargeForLesson(5, 2, 4));
        assertEquals(0, TuskCompat.chargeForLesson(4, 0, 0));
    }

    @Test
    void noopIsInactive() {
        assertEquals(false, ITuskCompat.NOOP.isActive());
    }
}
