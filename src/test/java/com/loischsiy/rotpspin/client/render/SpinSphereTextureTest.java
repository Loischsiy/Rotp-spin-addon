package com.loischsiy.rotpspin.client.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.client.renderer.texture.NativeImage;

class SpinSphereTextureTest {
    @Test
    void sizeAndOpaque() {
        NativeImage image = SpinSphereTexture.steelBall();
        assertEquals(SpinSphereTexture.WIDTH, image.getWidth());
        assertEquals(SpinSphereTexture.HEIGHT, image.getHeight());
        int rgba = image.getPixelRGBA(0, 0);
        assertEquals(255, NativeImage.getA(rgba));
        image.close();
    }

    @Test
    void equatorBrighterThanPole() {
        NativeImage image = SpinSphereTexture.steelBall();
        int pole = brightness(image.getPixelRGBA(10, 0));
        int equator = brightness(image.getPixelRGBA(10, SpinSphereTexture.HEIGHT / 2));
        assertTrue(equator > pole);
        image.close();
    }

    @Test
    void wreckingBandIsBrassAtEquator() {
        NativeImage image = SpinSphereTexture.wreckingBall();
        int rgba = image.getPixelRGBA(4, SpinSphereTexture.HEIGHT / 2);
        int r = NativeImage.getR(rgba);
        int g = NativeImage.getG(rgba);
        int b = NativeImage.getB(rgba);
        // Brass: strong red, medium green, almost no blue.
        assertTrue(r > 100 && g > 50 && g < r && b < 60);
        // Same texel on plain steel is grayish, not brass.
        NativeImage steel = SpinSphereTexture.steelBall();
        int s = steel.getPixelRGBA(4, SpinSphereTexture.HEIGHT / 2);
        assertTrue(Math.abs(NativeImage.getR(s) - NativeImage.getB(s)) < 60);
        image.close();
        steel.close();
    }

    private static int brightness(int rgba) {
        return NativeImage.getR(rgba) + NativeImage.getG(rgba) + NativeImage.getB(rgba);
    }
}
