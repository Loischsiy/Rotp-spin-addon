package com.loischsiy.rotpspin.power;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SpinLessonsTest {

    @Test
    void startsAtLessonOne() {
        assertEquals(1, SpinLessons.reached(1, 0, 0, 10, 5));
    }

    @Test
    void ballHitsTeachLessonTwo() {
        assertEquals(1, SpinLessons.reached(1, 9, 0, 10, 5));
        assertEquals(2, SpinLessons.reached(1, 10, 0, 10, 5));
    }

    @Test
    void hijacksTeachLessonThreeOnlyAfterTwo() {
        assertEquals(2, SpinLessons.reached(2, 10, 4, 10, 5));
        assertEquals(3, SpinLessons.reached(2, 10, 5, 10, 5));
        // enough hijacks but lesson 2 not learned: stops at 2 in the same step, never skips it
        assertEquals(1, SpinLessons.reached(1, 0, 99, 10, 5));
    }

    @Test
    void bothRequirementsAtOnceGoStraightToThree() {
        assertEquals(3, SpinLessons.reached(1, 10, 5, 10, 5));
    }

    @Test
    void neverGoesBackOrBeyondMax() {
        assertEquals(3, SpinLessons.reached(3, 0, 0, 10, 5));
        assertEquals(3, SpinLessons.reached(7, 0, 0, 10, 5));
        assertEquals(1, SpinLessons.reached(0, 0, 0, 10, 5));
    }

    @Test
    void zeroRequirementsUnlockEverything() {
        assertEquals(3, SpinLessons.reached(1, 0, 0, 0, 0));
    }
}
