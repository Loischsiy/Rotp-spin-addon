package com.loischsiy.rotpspin.client;

import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.client.render.SpunItemRenderer;
import com.loischsiy.rotpspin.client.render.SteelBallRenderer;
import com.loischsiy.rotpspin.init.InitEntities;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = AddonMain.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientInit {

    @SubscribeEvent
    public static void onFMLClientSetup(FMLClientSetupEvent event) {
        // Must NOT be inside event.enqueueWork(): in Forge 36 RenderingRegistry.loadEntityRenderers
        // runs right after this event and before deferred work, so a deferred registration is lost
        // and the entity has no renderer (NPE in EntityRendererManager.render). Same as RotP's ClientSetup.
        RenderingRegistry.registerEntityRenderingHandler(InitEntities.STEEL_BALL.get(), SteelBallRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(InitEntities.SPUN_ITEM.get(), SpunItemRenderer::new);
        // Key bindings are not thread-safe (they edit GameSettings.keyMappings): deferred, as RotP's ClientSetup does.
        event.enqueueWork(SpinKeys::register);
    }
}
