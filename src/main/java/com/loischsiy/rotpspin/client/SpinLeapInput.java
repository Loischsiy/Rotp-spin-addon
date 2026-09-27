package com.loischsiy.rotpspin.client;

import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClOnLeapPacket;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mod.IPlayerLeap;
import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.init.InitPowers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.MovementInput;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * Lesson 1 leap (Shift + Jump on the ground) regardless of the RotP HUD mode.
 * <p>
 * RotP's {@code InputHandler#onInputUpdate} leaps only with the power whose HUD mode is open,
 * so with the HUD closed or in the Stand mode the Spin leap never fired. Spin on one's own body
 * is not a hotbar technique, so this handler covers the other modes with the same pipeline as RotP:
 * client impulse via {@link MCUtil#leap} + {@link ClOnLeapPacket}, whose server handler checks
 * {@code canLeap()}, consumes energy and sets the cooldown.
 * <p>
 * Runs after RotP (LOWEST vs LOW): if RotP already leaped, it has cleared the jump/sneak input,
 * so there is no double leap; a Stand that can leap keeps priority in the Stand mode.
 */
@EventBusSubscriber(modid = AddonMain.MOD_ID, value = Dist.CLIENT)
public class SpinLeapInput {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onInputUpdate(InputUpdateEvent event) {
        MovementInput input = event.getMovementInput();
        if (!input.jumping || !input.shiftKeyDown) {
            return;
        }
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player == null || event.getPlayer() != player
                || !player.isOnGround() || player.isPassenger() || player.isFallFlying()) {
            return;
        }
        ActionsOverlayGui hud = ActionsOverlayGui.getInstance();
        if (hud != null && hud.getCurrentMode() == PowerClassification.NON_STAND) {
            return; // RotP's own leap handles the non-stand mode
        }
        INonStandPower.getNonStandPowerOptional(player).ifPresent(power -> {
            if (power.getType() != InitPowers.SPIN.get() || !power.canLeap()) {
                return;
            }
            float strength = power.leapStrength();
            if (strength <= 0) {
                return;
            }
            input.shiftKeyDown = false;
            input.jumping = false;
            PacketManager.sendToServer(new ClOnLeapPacket(PowerClassification.NON_STAND));
            IPlayerLeap.onLeapFixWrongMovement(player);
            MCUtil.leap(player, strength);
        });
    }
}
