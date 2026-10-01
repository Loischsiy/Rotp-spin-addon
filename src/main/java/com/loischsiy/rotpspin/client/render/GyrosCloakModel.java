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
 * Gyro's cloak worn on the player (texture 64x32, Box UV, docs/art/gyros_cloak.md section 2).
 * Both parts hang from the torso pivot (the neck) and copy the torso pose, so sneaking tilts them too.
 */
@OnlyIn(Dist.CLIENT)
public class GyrosCloakModel extends Model {
    private final ModelRenderer cloak;

    public GyrosCloakModel() {
        super(RenderType::entityCutoutNoCull);
        texWidth = 64;
        texHeight = 32;

        cloak = new ModelRenderer(this);
        cloak.setPos(0.0F, 0.0F, 0.0F);
        cloak.texOffs(0, 0).addBox(-5.0F, 0.0F, 2.2F, 10.0F, 16.0F, 1.0F, 0.0F, false);   // back panel, UV 22x17
        cloak.texOffs(0, 17).addBox(-4.5F, -0.5F, -2.5F, 9.0F, 2.0F, 5.0F, 0.3F, false);  // shoulder collar, UV 28x7
    }

    /** Fits the cloak to the wearer's current pose (called after the wearer's model setupAnim). */
    public void copyPose(BipedModel<?> wearer) {
        cloak.copyFrom(wearer.body);
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay,
            float red, float green, float blue, float alpha) {
        cloak.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
