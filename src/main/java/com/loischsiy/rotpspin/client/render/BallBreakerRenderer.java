package com.loischsiy.rotpspin.client.render;

import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandModelRegistry;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.StandEntityRenderer;
import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.entity.BallBreakerEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class BallBreakerRenderer extends StandEntityRenderer<BallBreakerEntity, StandEntityModel<BallBreakerEntity>> {

    public BallBreakerRenderer(EntityRendererManager renderManager) {
        super(renderManager,
                StandModelRegistry.registerModel(new ResourceLocation(AddonMain.MOD_ID, "ball_breaker"), BallBreakerModel::new),
                // Painted by the artist (docs/art/ball_breaker.md); missing until it lands.
                new ResourceLocation(AddonMain.MOD_ID, "textures/entity/stand/ball_breaker.png"), 0);
    }
}
