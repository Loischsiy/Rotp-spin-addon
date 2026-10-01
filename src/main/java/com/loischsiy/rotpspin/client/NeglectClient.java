package com.loischsiy.rotpspin.client;

import org.lwjgl.opengl.GL11;

import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.entity.WreckingBall;
import com.loischsiy.rotpspin.init.InitEffects;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * Hemispatial neglect, the local player's half (the server syncs a player's own effects to them):
 * the left half of the screen is veiled, and a neglected rider keeps steering the horse to the right.
 * The rider is turned rather than the horse because a player-ridden horse moves client-side and
 * copies the rider's yaw, and the effects of other entities are not synced to the client.
 */
@EventBusSubscriber(modid = AddonMain.MOD_ID, value = Dist.CLIENT)
public class NeglectClient {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (event.phase != TickEvent.Phase.END || event.player != player
                || !(player.getVehicle() instanceof AbstractHorseEntity)
                || !player.hasEffect(InitEffects.NEGLECT.get())) {
            return;
        }
        AbstractHorseEntity horse = (AbstractHorseEntity) player.getVehicle();
        if (horse.getControllingPassenger() == player) {
            // yRotO keeps the old value: the camera turns smoothly into the drift.
            player.yRot = WreckingBall.veerRight(player.yRot, SpinConfig.WRECKING_NEGLECT_VEER_DEGREES.get());
        }
    }

    /** Drawn with the helmet overlay, like RotP's own vision loss: under the hotbar and chat. */
    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        ClientPlayerEntity player = mc.player;
        if (event.getType() != RenderGameOverlayEvent.ElementType.HELMET || player == null || player.isSpectator()) {
            return;
        }
        EffectInstance neglect = player.getEffect(InitEffects.NEGLECT.get());
        if (neglect == null) {
            return;
        }
        float opacity = WreckingBall.neglectVeilOpacity(neglect.getDuration(),
                SpinConfig.WRECKING_NEGLECT_VEIL_FADE_OUT_TICKS.get(), SpinConfig.WRECKING_NEGLECT_VEIL_OPACITY.get());
        if (opacity <= 0.0F) {
            return;
        }
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();
        float centre = width / 2.0F;
        float fadeStart = Math.max(0.0F, centre - (float) (width * SpinConfig.WRECKING_NEGLECT_VEIL_FADE_WIDTH.get()));
        renderVeil(event.getMatrixStack(), fadeStart, centre, height, opacity);
    }

    /** Solid black from the left edge to {@code fadeStart}, then a horizontal fade to clear at {@code clearAt}. */
    private static void renderVeil(MatrixStack matrixStack, float fadeStart, float clearAt, float height, float opacity) {
        Matrix4f pose = matrixStack.last().pose();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        BufferBuilder buffer = Tessellator.getInstance().getBuilder();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        // Same corner order as AbstractGui#fillGradient.
        quad(buffer, pose, 0.0F, fadeStart, height, opacity, opacity);
        quad(buffer, pose, fadeStart, clearAt, height, opacity, 0.0F);
        Tessellator.getInstance().end();
        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    private static void quad(BufferBuilder buffer, Matrix4f pose, float left, float right, float height,
            float leftAlpha, float rightAlpha) {
        if (right <= left) {
            return;
        }
        buffer.vertex(pose, right, 0.0F, 0.0F).color(0.0F, 0.0F, 0.0F, rightAlpha).endVertex();
        buffer.vertex(pose, left, 0.0F, 0.0F).color(0.0F, 0.0F, 0.0F, leftAlpha).endVertex();
        buffer.vertex(pose, left, height, 0.0F).color(0.0F, 0.0F, 0.0F, leftAlpha).endVertex();
        buffer.vertex(pose, right, height, 0.0F).color(0.0F, 0.0F, 0.0F, rightAlpha).endVertex();
    }
}
