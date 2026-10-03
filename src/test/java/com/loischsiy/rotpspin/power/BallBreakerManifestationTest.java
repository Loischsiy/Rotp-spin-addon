package com.loischsiy.rotpspin.power;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BallBreakerManifestationTest {

    @Test
    void masterOnGallopWithPerfectBallManifests() {
        assertTrue(BallBreakerManifestation.canManifest(true, 5, 5, true, true, true, false));
    }

    @Test
    void everyMissingConditionBlocks() {
        assertFalse(BallBreakerManifestation.canManifest(false, 5, 5, true, true, true, false), "disabled");
        assertFalse(BallBreakerManifestation.canManifest(true, 4, 5, true, true, true, false), "lesson too low");
        assertFalse(BallBreakerManifestation.canManifest(true, 5, 5, false, true, true, false), "no gallop Super Spin");
        assertFalse(BallBreakerManifestation.canManifest(true, 5, 5, true, false, true, false), "chipped or not a steel ball");
        assertFalse(BallBreakerManifestation.canManifest(true, 5, 5, true, true, false, false), "no spin");
        assertFalse(BallBreakerManifestation.canManifest(true, 5, 5, true, true, true, true), "already has a Stand");
    }

    @Test
    void chippedBallWeakensSenescence() {
        org.junit.jupiter.api.Assertions.assertEquals(0.5, BallBreakerManifestation.senescenceScale(true, 0.5), 1e-9);
        org.junit.jupiter.api.Assertions.assertEquals(1.0, BallBreakerManifestation.senescenceScale(false, 0.5), 1e-9);
    }

    @Test
    void minLessonIsConfigurable() {
        assertTrue(BallBreakerManifestation.canManifest(true, 4, 4, true, true, true, false));
    }
}
