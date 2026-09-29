package com.loischsiy.rotpspin.client.render;

import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.entity.SteelBallEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

/**
 * A spinning ball is an exact math sphere with a procedural wraparound skin (steel engraving,
 * brass band on the guard version), rotating around the axis perpendicular to the flight —
 * like a rolling wheel. No billboard sprite: the rotation reads from every camera angle.
 */
public class SteelBallRenderer extends EntityRenderer<SteelBallEntity> {
    // Visual only, not gameplay: the sphere reads better slightly bigger than the hitbox.
    private static final SpinSphere MESH = SpinSphere.build(0.19F, 24, 16);
    private static final float SATELLITE_SCALE = 0.55F;
    private static final float DEGREES_PER_TICK = 40.0F;

    private ResourceLocation steelTexture;
    private ResourceLocation wreckingTexture;

    public SteelBallRenderer(EntityRendererManager manager) {
        super(manager);
    }

    @Override
    public void render(SteelBallEntity entity, float yaw, float partialTicks, MatrixStack matrixStack,
            IRenderTypeBuffer buffer, int packedLight) {
        matrixStack.pushPose();
        float scale = entity.isSatellite() ? SATELLITE_SCALE : 1.0F;
        matrixStack.scale(scale, scale, scale);
        matrixStack.mulPose(spinRotation(entity, partialTicks));

        IVertexBuilder builder = buffer.getBuffer(RenderType.entityCutoutNoCull(texture(entity)));
        Matrix4f pose = matrixStack.last().pose();
        Matrix3f normal = matrixStack.last().normal();
        int[] indices = MESH.indices;
        float[] positions = MESH.positions;
        float[] normals = MESH.normals;
        float[] uvs = MESH.uvs;
        for (int i = 0; i < indices.length; i++) {
            int v = indices[i];
            builder.vertex(pose, positions[v * 3], positions[v * 3 + 1], positions[v * 3 + 2])
                    .color(255, 255, 255, 255)
                    .uv(uvs[v * 2], uvs[v * 2 + 1])
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(packedLight)
                    .normal(normal, normals[v * 3], normals[v * 3 + 1], normals[v * 3 + 2])
                    .endVertex();
        }
        matrixStack.popPose();
        super.render(entity, yaw, partialTicks, matrixStack, buffer, packedLight);
    }

    /** Spin around the axis perpendicular to the velocity, like a rolling wheel. */
    static Quaternion spinRotation(SteelBallEntity entity, float partialTicks) {
        Vector3d velocity = entity.getDeltaMovement();
        return new Quaternion(spinAxis(velocity.x, velocity.z),
                (entity.tickCount + partialTicks) * DEGREES_PER_TICK, true);
    }

    /**
     * Horizontal axis perpendicular to the flight ({@code velocity x up}), unit length.
     * Pure math for tests: straight up/down flight falls back to the X axis.
     */
    static Vector3f spinAxis(double velocityX, double velocityZ) {
        float axisX = (float) -velocityZ;
        float axisZ = (float) velocityX;
        float length = (float) Math.sqrt(axisX * axisX + axisZ * axisZ);
        if (length < 1e-4F) {
            return new Vector3f(1.0F, 0.0F, 0.0F);
        }
        return new Vector3f(axisX / length, 0.0F, axisZ / length);
    }

    private ResourceLocation texture(SteelBallEntity entity) {
        if (entity.isWrecking()) {
            if (wreckingTexture == null) {
                wreckingTexture = register("wrecking_ball_sphere", SpinSphereTexture.wreckingBall());
            }
            return wreckingTexture;
        }
        if (steelTexture == null) {
            steelTexture = register("steel_ball_sphere", SpinSphereTexture.steelBall());
        }
        return steelTexture;
    }

    private static ResourceLocation register(String name, NativeImage image) {
        ResourceLocation location = new ResourceLocation(AddonMain.MOD_ID, "dynamic/" + name);
        Minecraft.getInstance().getTextureManager().register(location, new DynamicTexture(image));
        return location;
    }

    @Override
    public ResourceLocation getTextureLocation(SteelBallEntity entity) {
        return texture(entity);
    }
}
