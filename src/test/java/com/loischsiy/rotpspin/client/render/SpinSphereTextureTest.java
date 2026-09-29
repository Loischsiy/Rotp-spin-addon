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
    void steelIsGrayish() {
        NativeImage steel = SpinSphereTexture.steelBall();
        int s = steel.getPixelRGBA(4, SpinSphereTexture.HEIGHT / 2);
        assertTrue(Math.abs(NativeImage.getR(s) - NativeImage.getB(s)) < 60);
        steel.close();
    }

    @Test
    void wreckingBodyIsCopperWithOrangeGrooves() {
        NativeImage image = SpinSphereTexture.wreckingBall();
        int row = SpinSphereTexture.HEIGHT / 2;
        // Body between grooves: dark copper, red > green > blue.
        int body = image.getPixelRGBA(SpinSphereTexture.WIDTH / 2 - 3, row);
        assertTrue(NativeImage.getR(body) > 100 && NativeImage.getR(body) > NativeImage.getG(body)
                && NativeImage.getG(body) > NativeImage.getB(body));
        // The hottest pixel of the row is an orange groove: strong red, medium green, little blue.
        int hottest = 0;
        int hottestScore = -1;
        for (int x = 0; x < SpinSphereTexture.WIDTH; x++) {
            int rgba = image.getPixelRGBA(x, row);
            int score = NativeImage.getR(rgba) - NativeImage.getB(rgba);
            if (score > hottestScore) {
                hottestScore = score;
                hottest = rgba;
            }
        }
        assertTrue(NativeImage.getR(hottest) > 200 && NativeImage.getG(hottest) > 80 && NativeImage.getB(hottest) < 130);
        image.close();
    }

    @Test
    void satelliteIsGold() {
        NativeImage image = SpinSphereTexture.satelliteBall();
        assertEquals(SpinSphereTexture.SMALL_WIDTH, image.getWidth());
        int rgba = image.getPixelRGBA(20, SpinSphereTexture.SMALL_HEIGHT / 2);
        assertTrue(NativeImage.getR(rgba) > 200 && NativeImage.getG(rgba) > 150 && NativeImage.getB(rgba) < 100);
        image.close();
    }

    @Test
    void emptySocketIsDark() {
        NativeImage image = SpinSphereTexture.emptySocket();
        int rgba = image.getPixelRGBA(3, 3);
        assertTrue(brightness(rgba) < 150);
        assertEquals(255, NativeImage.getA(rgba));
        image.close();
    }

    private static int brightness(int rgba) {
        return NativeImage.getR(rgba) + NativeImage.getG(rgba) + NativeImage.getB(rgba);
    }
}
