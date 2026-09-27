package com.loischsiy.rotpspin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AddonMainTest {

    @Test
    void modIdMatchesModsToml() {
        assertEquals("rotp_spin", AddonMain.MOD_ID);
    }
}
