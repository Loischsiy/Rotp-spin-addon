package com.loischsiy.rotpspin.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Gyro's cloak worn on the player (texture 64x32, Box UV, docs/art/gyros_cloak.md section 2):
 * a shoulder piece that splits into three segments (JoJo Wiki, Gyro Zeppeli: Appearance).
 * The shoulder piece copies the torso pose; each segment swings on its own hinge under it.
 * The outer segments share one UV area, the right one mirrored, so both sides match to the pixel.
 */
@OnlyIn(Dist.CLIENT)
public class GyrosCloakModel extends Model {
    private static final float DEG = (float) (Math.PI / 180.0);
    private final ModelRenderer shoulders;
    private final ModelRenderer[] segments = new ModelRenderer[CloakAnim.SEGMENTS];

    public GyrosCloakModel() {
        super(RenderType::entityCutoutNoCull);
        texWidth = 64;
        texHeight = 32;

        shoulders = new ModelRenderer(this);
        shoulders.setPos(0.0F, 0.0F, 0.0F);
        shoulders.texOffs(0, 0).addBox(-4.5F, -0.5F, -2.5F, 9.0F, 3.0F, 5.0F, 0.3F, false); // UV 28x8
        segments[0] = segment(-3.5F, 0, 8, 3, 11, false); // UV 8x12
        segments[1] = segment(0.0F, 8, 8, 4, 13, false);  // UV 10x14
        segments[2] = segment(3.5F, 0, 8, 3, 11, true);   // mirror of segment 0
    }

    private ModelRenderer segment(float x, int u, int v, int width, int height, boolean mirror) {
        ModelRenderer part = new ModelRenderer(this);
        part.setPos(x, 1.5F, 2.4F); // hinge under the shoulder piece, just behind the back
        part.texOffs(u, v).addBox(-width / 2.0F, 0.0F, 0.0F, width, height, 1.0F, 0.0F, mirror);
        shoulders.addChild(part);
        return part;
    }

    /** Fits the cloak to the wearer's torso and sets each segment's swing (degrees, see CloakAnim). */
    public void pose(BipedModel<?> wearer, float[] pitch, float[] roll) {
        shoulders.copyFrom(wearer.body);
        for (int i = 0; i < segments.length; i++) {
            segments[i].xRot = pitch[i] * DEG;
            segments[i].zRot = roll[i] * DEG;
        }
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay,
            float red, float green, float blue, float alpha) {
        shoulders.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
