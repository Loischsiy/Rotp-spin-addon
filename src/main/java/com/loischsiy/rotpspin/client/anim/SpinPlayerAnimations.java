package com.loischsiy.rotpspin.client.anim;

import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.BasicToggleAnim;
import com.loischsiy.rotpspin.AddonMain;

import net.minecraft.util.ResourceLocation;

/**
 * Player pose layers of the addon (client only). Without playerAnimator RotP hands out a no-op layer,
 * so the fields are never null after {@link #init()}; before it (or if RotP's animator is missing)
 * they are null and callers must check.
 */
public final class SpinPlayerAnimations {
    public static BasicToggleAnim bodyBrace;
    public static BasicToggleAnim ballCharge;

    private SpinPlayerAnimations() {}

    /** Run from FMLClientSetupEvent#enqueueWork: RotP creates its animator in its own client setup. */
    public static void init() {
        PlayerAnimationHandler.IPlayerAnimator animator = PlayerAnimationHandler.getPlayerAnimator();
        if (animator == null) {
            AddonMain.LOGGER.warn("RotP player animator is not initialised; Spin poses disabled");
            return;
        }
        bodyBrace = animator.registerBasicAnimLayer(
                "com.loischsiy.rotpspin.client.anim.KosmXBodyBraceHandler",
                new ResourceLocation(AddonMain.MOD_ID, "body_brace"), 1);
        ballCharge = animator.registerBasicAnimLayer(
                "com.loischsiy.rotpspin.client.anim.KosmXBallChargeHandler",
                new ResourceLocation(AddonMain.MOD_ID, "ball_charge"), 1);
    }

    public static boolean setBodyBrace(net.minecraft.entity.player.PlayerEntity player, boolean enabled) {
        return bodyBrace != null && bodyBrace.setAnimEnabled(player, enabled);
    }

    public static boolean setBallCharge(net.minecraft.entity.player.PlayerEntity player, boolean enabled) {
        return ballCharge != null && ballCharge.setAnimEnabled(player, enabled);
    }
}
