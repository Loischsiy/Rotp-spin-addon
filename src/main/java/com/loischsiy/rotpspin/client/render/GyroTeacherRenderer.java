package com.loischsiy.rotpspin.client.render;

import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.entity.GyroTeacherEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.util.ResourceLocation;

/**
 * The mentor renders as a human in Gyro's skin (painted by the artist, 64x64 player format):
 * no Gecko model needed, vanilla humanoid animation applies.
 */
public class GyroTeacherRenderer extends MobRenderer<GyroTeacherEntity, PlayerModel<GyroTeacherEntity>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(AddonMain.MOD_ID, "textures/entity/gyro_teacher.png");

    public GyroTeacherRenderer(EntityRendererManager renderManager) {
        super(renderManager, new PlayerModel<>(0.0F, false), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(GyroTeacherEntity entity) {
        return TEXTURE;
    }
}
