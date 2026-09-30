package com.loischsiy.rotpspin.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundNBT;

/**
 * Gyro's lessons 1-5 as pure practice counters (docs/spin-lore.md).
 * Default config thresholds apply (Forge returns defaults when unloaded):
 * lesson 2 after 10 ball hits, lesson 3 after 5 hijacks,
 * lesson 4 after 15 golden hits, lesson 5 after 30 golden hits (cumulative).
 */
class SpinDataTest {
    private SpinData data;

    @BeforeEach
    void setup() {
        data = new SpinData();
        data.setPower(mock(INonStandPower.class));
    }

    @Test
    void ballHitsTeachLessonTwo() {
        for (int i = 0; i < 9; i++) {
            data.onSpinBallHit();
            assertEquals(1, data.getLesson());
        }
        data.onSpinBallHit();
        assertEquals(2, data.getLesson());
        assertEquals(10, data.getBallHits());
    }

    @Test
    void hijacksTeachLessonThree() {
        learnLessonTwo();
        for (int i = 0; i < 4; i++) {
            data.onMuscleHijack();
            assertEquals(2, data.getLesson());
        }
        data.onMuscleHijack();
        assertEquals(3, data.getLesson());
        assertEquals(5, data.getHijacks());
    }

    @Test
    void goldenHitsTeachLessonsFourAndFive() {
        learnLessonTwo();
        for (int i = 0; i < 5; i++) {
            data.onMuscleHijack();
        }
        assertEquals(3, data.getLesson());
        for (int i = 0; i < 14; i++) {
            data.onGoldenHit();
            assertEquals(3, data.getLesson());
        }
        data.onGoldenHit();
        assertEquals(4, data.getLesson());
        for (int i = 0; i < 14; i++) {
            data.onGoldenHit();
        }
        assertEquals(4, data.getLesson());
        data.onGoldenHit();
        assertEquals(5, data.getLesson());
        assertEquals(30, data.getGoldenHits());
    }

    @Test
    void countersOnlyCountAtTheirLesson() {
        data.onMuscleHijack();
        assertEquals(0, data.getHijacks());
        data.onGoldenHit();
        assertEquals(0, data.getGoldenHits());
        data.setLesson(4, false);
        data.onSpinBallHit();
        assertEquals(0, data.getBallHits());
    }

    @Test
    void takingLessonBackResetsPractice() {
        learnLessonTwo();
        data.setLesson(1, false);
        assertEquals(1, data.getLesson());
        assertEquals(0, data.getBallHits());
        assertEquals(0, data.getHijacks());
        assertEquals(0, data.getGoldenHits());
    }

    @Test
    void lessonClamps() {
        data.setLesson(99, false);
        assertEquals(SpinLessons.MAX, data.getLesson());
        data.setLesson(-3, false);
        assertEquals(SpinLessons.FIRST, data.getLesson());
    }

    @Test
    void nbtRoundTrip() {
        learnLessonTwo();
        data.onMuscleHijack();
        CompoundNBT nbt = data.writeNBT();
        SpinData read = new SpinData();
        read.setPower(mock(INonStandPower.class));
        read.readNBT(nbt);
        assertEquals(2, read.getLesson());
        assertEquals(data.getBallHits(), read.getBallHits());
        assertEquals(1, read.getHijacks());
    }

    private void learnLessonTwo() {
        for (int i = 0; i < 10; i++) {
            data.onSpinBallHit();
        }
        assertEquals(2, data.getLesson());
    }
}
