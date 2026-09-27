package com.loischsiy.rotpspin.client;

import org.lwjgl.glfw.GLFW;

import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.item.GyrosHolsterItem;
import com.loischsiy.rotpspin.network.AddonPackets;
import com.loischsiy.rotpspin.network.c2s.HolsterThrowPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * Addon key bindings (pattern: RotP {@code InputHandler}). Rebindable in Options -> Controls,
 * category "RotP Spin".
 */
@EventBusSubscriber(modid = AddonMain.MOD_ID, value = Dist.CLIENT)
public class SpinKeys {
    public static final String CATEGORY = "key.categories.rotp_spin";

    private static KeyBinding holsterThrow;

    // Called from FMLClientSetupEvent#enqueueWork, as RotP does
    public static void register() {
        holsterThrow = new KeyBinding(GyrosHolsterItem.THROW_KEY, KeyConflictContext.IN_GAME,
                InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY);
        ClientRegistry.registerKeyBinding(holsterThrow);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || holsterThrow == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        boolean canAct = mc.player != null && mc.screen == null && mc.overlay == null;
        while (holsterThrow.consumeClick()) {
            if (canAct) {
                AddonPackets.sendToServer(new HolsterThrowPacket());
                canAct = false; // one throw per tick; the item cooldown limits the rest on the server
            }
        }
    }
}
