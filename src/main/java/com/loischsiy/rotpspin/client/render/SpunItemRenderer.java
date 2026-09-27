package com.loischsiy.rotpspin.client.render;

import com.loischsiy.rotpspin.entity.SpunItemEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

/** The thrown item, facing the camera and rotating (same approach as {@link SteelBallRenderer}). */
public class SpunItemRenderer extends EntityRenderer<SpunItemEntity> {
    // visual only
    private static final float DEGREES_PER_TICK = 40.0F;

    public SpunItemRenderer(EntityRendererManager manager) {
        super(manager);
    }

    @Override
    public void render(SpunItemEntity entity, float yaw, float partialTicks, MatrixStack matrixStack,
            IRenderTypeBuffer buffer, int packedLight) {
        ItemStack stack = entity.getItem();
        if (!stack.isEmpty()) {
            matrixStack.pushPose();
            matrixStack.mulPose(entityRenderDispatcher.cameraOrientation());
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees((entity.tickCount + partialTicks) * DEGREES_PER_TICK));
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemCameraTransforms.TransformType.GROUND,
                    packedLight, OverlayTexture.NO_OVERLAY, matrixStack, buffer);
            matrixStack.popPose();
        }
        super.render(entity, yaw, partialTicks, matrixStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(SpunItemEntity entity) {
        return AtlasTexture.LOCATION_BLOCKS;
    }
}
