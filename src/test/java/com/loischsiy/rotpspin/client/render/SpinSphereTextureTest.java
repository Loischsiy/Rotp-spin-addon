package com.loischsiy.rotpspin.client.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import net.minecraft.client.renderer.texture.NativeImage;

class SpinSphereTextureTest {
    private static final String STEEL_WRAP = "/assets/rotp_spin/textures/entity/steel_ball_wrapped.png";
    private static final String STEEL_ICON = "/assets/rotp_spin/textures/item/steel_ball.png";

    private static BufferedImage resource(String path) throws IOException {
        try (InputStream in = SpinSphereTextureTest.class.getResourceAsStream(path)) {
            assertNotNull(in, path);
            return ImageIO.read(in);
        }
    }

    @Test
    void steelWrapSizeAndOpaque() throws IOException {
        BufferedImage image = resource(STEEL_WRAP);
        assertEquals(256, image.getWidth());
        assertEquals(128, image.getHeight());
        for (int y = 0; y < image.getHeight(); y += 7) {
            for (int x = 0; x < image.getWidth(); x += 5) {
                assertEquals(255, image.getRGB(x, y) >>> 24);
            }
        }
    }

    @Test
    void steelWrapIsGreenMetalWithDarkGrooves() throws IOException {
        BufferedImage image = resource(STEEL_WRAP);
        long green = 0;
        long dark = 0;
        long total = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 255;
                int g = (rgb >> 8) & 255;
                int b = rgb & 255;
                total++;
                if (g > r + 20 && g > b + 20) {
                    green++;
                }
                if (g < 60) {
                    dark++;
                }
            }
        }
        // Almost every pixel is green; the hexagon frame and slits add a few percent of near-black.
        assertTrue(green > total * 0.95, "green share " + green + "/" + total);
        assertTrue(dark > total / 200 && dark < total / 4, "dark share " + dark + "/" + total);
    }

    @Test
    void steelWrapHexagonFacesPlusZ() throws IOException {
        // u = 0.25 on the equator is +Z: the centre of the hexagon plate, lighter than the dark
        // frame groove a little to the side of it.
        BufferedImage image = resource(STEEL_WRAP);
        int row = image.getHeight() / 2;
        int centre = image.getWidth() / 4;
        int plate = brightness(new Color(image.getRGB(centre, row)));
        assertTrue(plate > 200, "plate " + plate);
        int darkest = Integer.MAX_VALUE;
        for (int x = centre; x < centre + image.getWidth() / 8; x++) {
            darkest = Math.min(darkest, brightness(new Color(image.getRGB(x, row))));
        }
        assertTrue(darkest < plate / 2, "groove " + darkest + " vs plate " + plate);
    }

    @Test
    void steelIconIsSixteenAndGreen() throws IOException {
        BufferedImage icon = resource(STEEL_ICON);
        assertEquals(16, icon.getWidth());
        assertEquals(16, icon.getHeight());
        int rgb = icon.getRGB(6, 8);
        assertEquals(255, rgb >>> 24);
        assertTrue(((rgb >> 8) & 255) > ((rgb >> 16) & 255) + 20);
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

    /** ImageIO packs ARGB, NativeImage packs ABGR: sum of channels is order-independent. */
    private static int brightness(Color c) {
        return c.getRed() + c.getGreen() + c.getBlue();
    }
}
