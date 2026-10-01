package com.loischsiy.rotpspin.client.render;

import java.util.Map;
import java.util.WeakHashMap;

import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.client.SpinSailClient;
import com.loischsiy.rotpspin.item.SteelBallItem;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Draws the worn cloak in the wearer's model space. Shared by the chest-slot layer
 * (GyrosCloakLayer) and the Curios back slot (CloakCurio); knows nothing about either.
 * Motion comes from CloakAnim; only the sail open/fold progress is kept per wearer.
 */
@OnlyIn(Dist.CLIENT)
public final class GyrosCloakRender {
    public static final ResourceLocation TEXTURE = new ResourceLocation(AddonMain.MOD_ID, "textures/entity/gyros_cloak.png");
    /** Other players: the sail flag is not synced, so a ball in hand and this descent (blocks per tick) mean a sail. */
    private static final double REMOTE_SAIL_DESCENT = 0.04D;
    private static final Map<LivingEntity, float[]> OPEN = new WeakHashMap<>(); // {open 0..1, last age}
    private static final float[] PITCH = new float[CloakAnim.SEGMENTS];
    private static final float[] ROLL = new float[CloakAnim.SEGMENTS];
    private static GyrosCloakModel model;

    private GyrosCloakRender() {}

    public static void render(MatrixStack matrixStack, IRenderTypeBuffer buffers, int light, LivingEntity wearer,
            float partialTicks, float ageInTicks) {
        EntityRenderer<? super LivingEntity> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(wearer);
        if (!(renderer instanceof LivingRenderer) || !(((LivingRenderer<?, ?>) renderer).getModel() instanceof BipedModel)) {
            return; // the cloak is fitted to the biped torso only
        }
        if (model == null) {
            model = new GyrosCloakModel();
        }
        animate(wearer, partialTicks, ageInTicks);
        model.pose((BipedModel<?>) ((LivingRenderer<?, ?>) renderer).getModel(), PITCH, ROLL);
        model.renderToBuffer(matrixStack, buffers.getBuffer(model.renderType(TEXTURE)), light, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void animate(LivingEntity wearer, float pt, float age) {
        // Cloth lag behind the body: players have the vanilla cape trail, other wearers use their own motion.
        double lagX;
        double lagY;
        double lagZ;
        float bob = 0.0F;
        if (wearer instanceof PlayerEntity) {
            PlayerEntity p = (PlayerEntity) wearer;
            lagX = MathHelper.lerp(pt, p.xCloakO, p.xCloak) - MathHelper.lerp(pt, p.xo, p.getX());
            lagY = MathHelper.lerp(pt, p.yCloakO, p.yCloak) - MathHelper.lerp(pt, p.yo, p.getY());
            lagZ = MathHelper.lerp(pt, p.zCloakO, p.zCloak) - MathHelper.lerp(pt, p.zo, p.getZ());
            bob = MathHelper.lerp(pt, p.oBob, p.bob);
        }
        else {
            lagX = wearer.xo - wearer.getX();
            lagY = wearer.yo - wearer.getY();
            lagZ = wearer.zo - wearer.getZ();
        }
        float yaw = MathHelper.lerp(pt, wearer.yBodyRotO, wearer.yBodyRot) * ((float) Math.PI / 180.0F);
        double sin = MathHelper.sin(yaw);
        double cos = -MathHelper.cos(yaw);
        float drag = CloakAnim.drag(lagX * sin + lagZ * cos, lagY, bob,
                MathHelper.lerp(pt, wearer.walkDistO, wearer.walkDist));
        float lean = CloakAnim.lean(lagX * cos - lagZ * sin);

        float[] state = OPEN.computeIfAbsent(wearer, w -> new float[] { 0.0F, age });
        float dt = age - state[1];
        if (dt < 0.0F || dt > 20.0F) {
            dt = 0.0F; // came back into view or the clock jumped: no catch-up
        }
        state[0] = CloakAnim.approach(state[0], isSailing(wearer) ? 1.0F : 0.0F, dt * CloakAnim.OPEN_PER_TICK);
        state[1] = age;
        for (int i = 0; i < CloakAnim.SEGMENTS; i++) {
            PITCH[i] = CloakAnim.pitch(i, drag, state[0], age);
            ROLL[i] = CloakAnim.roll(i, lean, state[0]);
        }
    }

    private static boolean isSailing(LivingEntity wearer) {
        if (wearer == Minecraft.getInstance().player) {
            return SpinSailClient.isSailing();
        }
        return !wearer.isOnGround() && !wearer.isInWater() && wearer.getY() - wearer.yo < -REMOTE_SAIL_DESCENT
                && (wearer.getMainHandItem().getItem() instanceof SteelBallItem
                        || wearer.getOffhandItem().getItem() instanceof SteelBallItem);
    }
}
