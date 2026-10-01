package com.loischsiy.rotpspin.client.render;

import com.loischsiy.rotpspin.AddonMain;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Draws the worn cloak in the wearer's model space. Shared by the chest-slot layer
 * (GyrosCloakLayer) and the Curios back slot (CloakCurio); knows nothing about either.
 */
@OnlyIn(Dist.CLIENT)
public final class GyrosCloakRender {
    public static final ResourceLocation TEXTURE = new ResourceLocation(AddonMain.MOD_ID, "textures/entity/gyros_cloak.png");
    private static GyrosCloakModel model;

    private GyrosCloakRender() {}

    public static void render(MatrixStack matrixStack, IRenderTypeBuffer buffers, int light, LivingEntity wearer) {
        EntityRenderer<? super LivingEntity> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(wearer);
        if (!(renderer instanceof LivingRenderer) || !(((LivingRenderer<?, ?>) renderer).getModel() instanceof BipedModel)) {
            return; // the cloak is fitted to the biped torso only
        }
        if (model == null) {
            model = new GyrosCloakModel();
        }
        model.copyPose((BipedModel<?>) ((LivingRenderer<?, ?>) renderer).getModel());
        model.renderToBuffer(matrixStack, buffers.getBuffer(model.renderType(TEXTURE)), light, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);
    }
}
