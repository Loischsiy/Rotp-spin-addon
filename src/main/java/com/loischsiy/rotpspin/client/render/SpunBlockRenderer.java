package com.loischsiy.rotpspin.client.render;

import com.loischsiy.rotpspin.entity.SpunBlockEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

/** The spinning block, rotating around Y (same spin as {@link SpunItemRenderer}). */
public class SpunBlockRenderer extends EntityRenderer<SpunBlockEntity> {
    // visual only
    private static final float DEGREES_PER_TICK = 40.0F;

    public SpunBlockRenderer(EntityRendererManager manager) {
        super(manager);
    }

    @Override
    public void render(SpunBlockEntity entity, float yaw, float partialTicks, MatrixStack matrixStack,
            IRenderTypeBuffer buffer, int packedLight) {
        BlockState state = entity.getBlockState();
        if (!state.isAir()) {
            matrixStack.pushPose();
            // Block models span 0..1, the entity position is the center.
            matrixStack.translate(-0.5D, 0.0D, -0.5D);
            matrixStack.mulPose(Vector3f.YP.rotationDegrees((entity.tickCount + partialTicks) * DEGREES_PER_TICK));
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                    state, matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
            matrixStack.popPose();
        }
        super.render(entity, yaw, partialTicks, matrixStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(SpunBlockEntity entity) {
        return AtlasTexture.LOCATION_BLOCKS;
    }
}
