package com.loischsiy.rotpspin.power;

import com.loischsiy.rotpspin.AddonMain;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/** Server: tracks the natural gallop of the Spin user's horse for Super Spin, see {@link SpinSuperSpin}. */
@EventBusSubscriber(modid = AddonMain.MOD_ID)
public class SuperSpinHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide()) {
            return;
        }
        SpinData.of(event.player).ifPresent(data -> data.tickHorseback(event.player));
    }
}
