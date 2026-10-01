package com.loischsiy.rotpspin.compat.tusk;

/**
 * Pure rules of Tusk ACT4 infinite rotation driven by Spin (docs/spin-lore.md, Tusk ACT4,
 * SBR ch. 85-87): the charge comes from Super Spin (horse at its natural gallop or the lesson 5
 * detour kick), and only a counter-rotation of the same order, a Super Spin ball, removes it.
 */
public final class TuskInfiniteRules {
    /** Tusk's own act index of ACT4 ({@code TuskCapability.getAct()} is 0-based). */
    public static final int ACT4_INDEX = 3;

    private TuskInfiniteRules() {
    }

    /** The Spin user's Tusk receives the infinite rotation charge this tick. */
    public static boolean grantsCharge(boolean enabled, boolean alreadyCharged, int act,
                                       int lesson, int minLesson, boolean superSpin) {
        return enabled && !alreadyCharged && act >= ACT4_INDEX && lesson >= minLesson && superSpin;
    }

    /** A ball hit unwinds infinite rotation on the target (counter-rotation, SBR ch. 86-87). */
    public static boolean counterRotates(boolean enabled, boolean spinning, boolean chipped,
                                         int lesson, int minLesson, boolean needsSuperSpin, boolean superSpin) {
        return enabled && spinning && !chipped && lesson >= minLesson && (!needsSuperSpin || superSpin);
    }
}
