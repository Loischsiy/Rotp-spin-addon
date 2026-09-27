package com.loischsiy.rotpspin.client.render;

import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.item.GyrosHolsterItem;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Draws the worn holster in the wearer's model space (called from a render layer, e.g. Curios).
 * Knows nothing about Curios, so any future wearing source can reuse it.
 */
@OnlyIn(Dist.CLIENT)
public final class GyrosHolsterRender {
    public static final ResourceLocation TEXTURE = new ResourceLocation(AddonMain.MOD_ID, "textures/entity/gyros_holster.png");
    private static GyrosHolsterModel model;

    private GyrosHolsterRender() {}

    public static void render(ItemStack holster, MatrixStack matrixStack, IRenderTypeBuffer buffers, int light, LivingEntity wearer) {
        EntityRenderer<? super LivingEntity> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(wearer);
        if (!(renderer instanceof LivingRenderer) || !(((LivingRenderer<?, ?>) renderer).getModel() instanceof BipedModel)) {
            return; // the belt is fitted to the biped torso only
        }
        if (model == null) {
            model = new GyrosHolsterModel();
        }
        BipedModel<?> biped = (BipedModel<?>) ((LivingRenderer<?, ?>) renderer).getModel();
        model.copyPose(biped);
        model.setBallCount(GyrosHolsterItem.getBallCount(holster));
        model.renderToBuffer(matrixStack, buffers.getBuffer(model.renderType(TEXTURE)), light, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);
    }
}
