package com.loischsiy.rotpspin.client.anim;

import com.github.standobyte.jojo.client.playeranim.anim.interfaces.BasicToggleAnim;
import com.github.standobyte.jojo.client.playeranim.kosmx.KosmXPlayerAnimatorInstalled;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXFixedFadeModifier;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.util.Ease;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

/**
 * Body Brace pose layer. References playerAnimator (KosmX) types, so it must only be loaded by name
 * through RotP's {@code registerBasicAnimLayer}, which does that only when playerAnimator is installed.
 * Same shape as RotP's own KosmXStoneFormHandler.
 */
public class KosmXBodyBraceHandler extends KosmXPlayerAnimatorInstalled.AnimLayerHandler<ModifierLayer<IAnimation>>
        implements BasicToggleAnim {
    private static final ResourceLocation POSE = new ResourceLocation("rotp_spin", "body_brace");

    public KosmXBodyBraceHandler(ResourceLocation id) {
        super(id);
    }

    @Override
    protected ModifierLayer<IAnimation> createAnimLayer(AbstractClientPlayerEntity player) {
        return new ModifierLayer<>(null);
    }

    @Override
    public boolean setAnimEnabled(PlayerEntity player, boolean enabled) {
        enabled &= !player.isPassenger(); // a leg stance on a horse looks broken
        if (enabled) {
            return setAnimFromName(player, POSE);
        }
        return fadeOutAnim(player, KosmXFixedFadeModifier.standardFadeIn(8, Ease.OUTCUBIC), null);
    }
}
