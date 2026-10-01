package com.loischsiy.rotpspin.client;

import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.power.SpinSail;
import com.loischsiy.rotpspin.power.SpinSailHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * Cloak as a sail, client half: the local player moves client-side, so the slowed glide is applied
 * here (like the vanilla elytra); the server only pays the energy and cancels the fall damage.
 */
@EventBusSubscriber(modid = AddonMain.MOD_ID, value = Dist.CLIENT)
public class SpinSailClient {
    private static boolean sailing;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (event.phase != TickEvent.Phase.END || event.player != player) {
            return;
        }
        boolean energy = player.abilities.instabuild
                || SpinSail.hasEnergy(sailing, ClientSpinState.getEnergy(), SpinConfig.SAIL_COST_PER_TICK.get(),
                        SpinConfig.SAIL_START_ENERGY.get());
        sailing = SpinSailHandler.canSail(player) && energy
                && SpinSail.shouldStart(sailing, player.fallDistance, SpinConfig.SAIL_MIN_FALL_DISTANCE.get());
        if (sailing && player.getDeltaMovement().y < 0.0D) {
            player.setDeltaMovement(SpinSail.sail(player.getDeltaMovement(), player.getLookAngle(),
                    SpinConfig.SAIL_MAX_FALL_SPEED.get(), SpinConfig.SAIL_FORWARD_PUSH.get(),
                    SpinConfig.SAIL_MAX_HORIZONTAL_SPEED.get()));
        }
    }
}
