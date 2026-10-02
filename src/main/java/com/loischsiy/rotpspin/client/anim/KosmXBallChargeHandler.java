package com.loischsiy.rotpspin.client.anim;

import com.github.standobyte.jojo.client.playeranim.anim.interfaces.BasicToggleAnim;
import com.github.standobyte.jojo.client.playeranim.kosmx.KosmXPlayerAnimatorInstalled;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXFixedFadeModifier;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXFixedMirrorModifier;
import com.loischsiy.rotpspin.action.SpinBallCharge;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.core.util.Vec3f;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;

/**
 * "Spin Charge" wind-up pose layer. The pose is authored for the right arm and mirrored when the ball
 * is in the left hand. Loaded by name through RotP's {@code registerBasicAnimLayer} only when
 * playerAnimator is installed (same shape as {@link KosmXBodyBraceHandler}).
 */
public class KosmXBallChargeHandler extends KosmXPlayerAnimatorInstalled.AnimLayerHandler<ModifierLayer<IAnimation>>
        implements BasicToggleAnim {
    private static final ResourceLocation POSE = new ResourceLocation("rotp_spin", "ball_charge");

    public KosmXBallChargeHandler(ResourceLocation id) {
        super(id);
    }

    @Override
    protected ModifierLayer<IAnimation> createAnimLayer(AbstractClientPlayerEntity player) {
        return new ModifierLayer<>(null, new BallHandMirror(player));
    }

    @Override
    public boolean setAnimEnabled(PlayerEntity player, boolean enabled) {
        enabled &= !player.isPassenger(); // the leg stance looks broken on a horse
        if (enabled) {
            return setAnimFromName(player, POSE);
        }
        return fadeOutAnim(player, KosmXFixedFadeModifier.standardFadeIn(5, Ease.OUTCUBIC), null);
    }

    /** Mirrors the right-arm pose when the steel ball is held by the left arm. */
    private static class BallHandMirror extends KosmXFixedMirrorModifier {
        private final AbstractClientPlayerEntity player;

        BallHandMirror(AbstractClientPlayerEntity player) {
            this.player = player;
        }

        @Override
        public Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f value0) {
            if (ballArm() == HandSide.RIGHT) {
                return anim == null ? value0 : anim.get3DTransform(modelName, type, tickDelta, value0);
            }
            return super.get3DTransform(modelName, type, tickDelta, value0);
        }

        private HandSide ballArm() {
            Hand hand = SpinBallCharge.handWithPlainBall(player);
            return hand == Hand.OFF_HAND ? player.getMainArm().getOpposite() : player.getMainArm();
        }
    }
}
