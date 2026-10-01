package com.loischsiy.rotpspin.client.render;

import com.loischsiy.rotpspin.item.GyrosCloakItem;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Player render layer: the cloak worn in the chest slot (the Curios back slot is drawn by Curios itself). */
@OnlyIn(Dist.CLIENT)
public class GyrosCloakLayer extends LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> {

    public GyrosCloakLayer(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> renderer) {
        super(renderer);
    }

    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffers, int light, AbstractClientPlayerEntity player,
            float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!player.isInvisible() && player.getItemBySlot(EquipmentSlotType.CHEST).getItem() instanceof GyrosCloakItem) {
            GyrosCloakRender.render(matrixStack, buffers, light, player);
        }
    }
}
