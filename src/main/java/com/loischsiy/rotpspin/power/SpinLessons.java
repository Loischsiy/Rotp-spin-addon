package com.loischsiy.rotpspin.power;

/**
 * Gyro's lessons (docs/spin-lore.md, "5 уроков Джайро"). Pure math, no World access.
 * <ul>
 * <li>1 "If there is a will, do it": Spin on one's own body and the steel ball (leap, throw);</li>
 * <li>2 "Use your muscles": Spin sent into another body (Muscle Hijack, Zeppeli Medicine);</li>
 * <li>3 "Believe in the rotation": Spin carried by inanimate matter (spinning items, ball control);</li>
 * <li>4 "Pay tribute. Spin the bullets in the golden ratio": Golden Spin, needs calibration;</li>
 * <li>5 "The shortest route is the detour": Super Spin, golden everywhere (see {@link SpinGolden}).</li>
 * </ul>
 */
public final class SpinLessons {
    public static final int FIRST = 1;
    public static final int MAX = 5;
    /** Lessons that only add a passive bonus, no new hotbar actions. */
    public static final int LAST_ACTION_LESSON = 3;

    private SpinLessons() {}

    public static int clamp(int lesson) {
        return Math.max(FIRST, Math.min(MAX, lesson));
    }

    /**
     * The lesson reached with the given practice. Lessons are learned in order: the practice of a
     * later lesson only counts once the previous one is known, which the caller guarantees by
     * counting each counter only while its lesson is in effect.
     */
    public static int reached(int current, int ballHits, int hijacks, int goldenHits,
            int lesson2BallHits, int lesson3Hijacks, int lesson4GoldenHits, int lesson5GoldenHits) {
        int lesson = clamp(current);
        if (lesson == 1 && ballHits >= lesson2BallHits) {
            lesson = 2;
        }
        if (lesson == 2 && hijacks >= lesson3Hijacks) {
            lesson = 3;
        }
        if (lesson == 3 && goldenHits >= lesson4GoldenHits) {
            lesson = 4;
        }
        if (lesson == 4 && goldenHits >= lesson5GoldenHits) {
            lesson = 5;
        }
        return lesson;
    }
}
