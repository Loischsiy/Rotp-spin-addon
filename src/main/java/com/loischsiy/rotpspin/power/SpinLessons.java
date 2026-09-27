package com.loischsiy.rotpspin.power;

/**
 * Gyro's lessons (docs/spin-lore.md, "5 уроков Джайро"). Pure math, no World access.
 * <ul>
 * <li>1 "If there is a will, do it": Spin on one's own body and the steel ball (leap, throw);</li>
 * <li>2 "Use your muscles": Spin sent into another body (Muscle Hijack, Zeppeli Medicine);</li>
 * <li>3 "Believe in the rotation": Spin carried by inanimate matter (spinning items, ball control).</li>
 * </ul>
 * Lessons 4-5 (Golden Spin, the detour) are not implemented yet.
 */
public final class SpinLessons {
    public static final int FIRST = 1;
    public static final int MAX = 3;

    private SpinLessons() {}

    public static int clamp(int lesson) {
        return Math.max(FIRST, Math.min(MAX, lesson));
    }

    /**
     * The lesson reached with the given practice. Lessons are learned in order: lesson 3 practice
     * (hijacks) only counts once lesson 2 is known, which the caller guarantees by counting hijacks
     * only while the technique is unlocked.
     */
    public static int reached(int current, int ballHits, int hijacks, int lesson2BallHits, int lesson3Hijacks) {
        int lesson = clamp(current);
        if (lesson == 1 && ballHits >= lesson2BallHits) {
            lesson = 2;
        }
        if (lesson == 2 && hijacks >= lesson3Hijacks) {
            lesson = 3;
        }
        return lesson;
    }
}
