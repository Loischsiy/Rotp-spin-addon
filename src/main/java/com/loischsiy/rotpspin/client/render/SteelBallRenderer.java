package com.loischsiy.rotpspin.client.render;

import com.loischsiy.rotpspin.entity.SteelBallEntity;
import com.loischsiy.rotpspin.init.InitItems;
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

/** Renders the ball as its item sprite facing the camera (as vanilla SpriteRenderer); a ball with Spin rotates faster. */
public class SteelBallRenderer extends EntityRenderer<SteelBallEntity> {
    // visual only
    private static final float SPIN_DEGREES_PER_TICK = 40.0F;
    private static final float PLAIN_DEGREES_PER_TICK = 12.0F;

    private ItemStack stack;

    public SteelBallRenderer(EntityRendererManager manager) {
        super(manager);
    }

    @Override
    public void render(SteelBallEntity entity, float yaw, float partialTicks, MatrixStack matrixStack,
            IRenderTypeBuffer buffer, int packedLight) {
        if (stack == null) {
            stack = new ItemStack(InitItems.STEEL_BALL.get());
        }
        matrixStack.pushPose();
        matrixStack.mulPose(entityRenderDispatcher.cameraOrientation());
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
        float degreesPerTick = entity.isSpinning() ? SPIN_DEGREES_PER_TICK : PLAIN_DEGREES_PER_TICK;
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees((entity.tickCount + partialTicks) * degreesPerTick));
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemCameraTransforms.TransformType.GROUND,
                packedLight, OverlayTexture.NO_OVERLAY, matrixStack, buffer);
        matrixStack.popPose();
        super.render(entity, yaw, partialTicks, matrixStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(SteelBallEntity entity) {
        return AtlasTexture.LOCATION_BLOCKS;
    }
}
