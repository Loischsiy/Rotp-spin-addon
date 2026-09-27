package com.loischsiy.rotpspin.power;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

class SpinLessonsTest {
    // reached(current, ballHits, hijacks, goldenHits, l2, l3, l4, l5)

    @Test
    void startsAtLessonOne() {
        assertEquals(1, SpinLessons.reached(1, 0, 0, 0, 10, 5, 15, 30));
    }

    @Test
    void ballHitsTeachLessonTwo() {
        assertEquals(1, SpinLessons.reached(1, 9, 0, 0, 10, 5, 15, 30));
        assertEquals(2, SpinLessons.reached(1, 10, 0, 0, 10, 5, 15, 30));
    }

    @Test
    void hijacksTeachLessonThreeOnlyAfterTwo() {
        assertEquals(2, SpinLessons.reached(2, 10, 4, 0, 10, 5, 15, 30));
        assertEquals(3, SpinLessons.reached(2, 10, 5, 0, 10, 5, 15, 30));
        // enough hijacks but lesson 2 not learned: stops at 2 in the same step, never skips it
        assertEquals(1, SpinLessons.reached(1, 0, 99, 0, 10, 5, 15, 30));
    }

    @Test
    void bothRequirementsAtOnceGoStraightToThree() {
        assertEquals(3, SpinLessons.reached(1, 10, 5, 0, 10, 5, 15, 30));
    }

    @Test
    void goldenHitsTeachLessonFourOnlyAfterThree() {
        assertEquals(3, SpinLessons.reached(3, 0, 0, 14, 10, 5, 15, 30));
        assertEquals(4, SpinLessons.reached(3, 0, 0, 15, 10, 5, 15, 30));
        // enough golden hits but lesson 3 not learned: never skips it
        assertEquals(2, SpinLessons.reached(2, 0, 0, 99, 10, 5, 15, 30));
    }

    @Test
    void goldenHitsTeachLessonFiveOnlyAfterFour() {
        assertEquals(4, SpinLessons.reached(4, 0, 0, 29, 10, 5, 15, 30));
        assertEquals(5, SpinLessons.reached(4, 0, 0, 30, 10, 5, 15, 30));
    }

    @Test
    void neverGoesBackOrBeyondMax() {
        assertEquals(5, SpinLessons.reached(5, 0, 0, 0, 10, 5, 15, 30));
        assertEquals(5, SpinLessons.reached(9, 0, 0, 0, 10, 5, 15, 30));
        assertEquals(1, SpinLessons.reached(0, 0, 0, 0, 10, 5, 15, 30));
    }

    @Test
    void zeroRequirementsUnlockEverything() {
        assertEquals(5, SpinLessons.reached(1, 0, 0, 0, 0, 0, 0, 0));
    }
}
