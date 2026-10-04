package com.loischsiy.rotpspin.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Gyro's belt worn on the player. Geometry is taken 1:1 from {@code docs/art/gyros_holster.bbmodel}
 * (Box UV, 64x64). Blockbench -> model space: the player's body pivot is BB y = 24,
 * so {@code y = 24 - to.y}; x and z are unchanged.
 * <p>
 * Two groups, because vanilla sneaking tilts the torso forward by 0.5 rad and lowers it by 3.2 px
 * while the legs stay upright ({@code BipedModel#setupAnim}):
 * <ul>
 * <li>{@code belt} (band + buckle) sits on the torso and copies its pose;</li>
 * <li>{@code hips} (straps, holsters, balls) hangs at the hips: it follows the leg pivots and stays
 * upright. Parented to the torso it swung 3-4 px down and into the thighs when sneaking.</li>
 * </ul>
 */
@OnlyIn(Dist.CLIENT)
public class GyrosHolsterModel extends Model {
    // Standing leg pivot of BipedModel (setupAnim, not sneaking)
    private static final float LEG_Y = 12.0F;
    private static final float LEG_Z = 0.1F;

    private final ModelRenderer belt;
    private final ModelRenderer hips;
    private final ModelRenderer ballRight;
    private final ModelRenderer ballLeft;
    private final ModelRenderer wreckingRight;
    private final ModelRenderer wreckingLeft;

    public GyrosHolsterModel() {
        super(RenderType::entityCutoutNoCull);
        texWidth = 64;
        texHeight = 64;

        belt = new ModelRenderer(this);
        belt.setPos(0.0F, 0.0F, 0.0F);
        belt.texOffs(0, 0).addBox(-4.0F, 10.0F, -2.0F, 8.0F, 2.0F, 4.0F, 0.6F, false);         // belt_band
        belt.texOffs(48, 8).addBox(-1.618F, 10.0F, -2.85F, 3.236F, 2.0F, 0.5F, 0.0F, false);   // buckle

        hips = new ModelRenderer(this);
        hips.setPos(0.0F, 0.0F, 0.0F);
        hips.texOffs(0, 8).addBox(2.5F, 12.0F, -4.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);          // holster_right
        hips.texOffs(24, 8).addBox(3.5F, 11.0F, -2.8F, 1.0F, 2.0F, 1.0F, 0.0F, false);         // strap_right
        hips.texOffs(12, 8).addBox(-5.5F, 12.0F, -4.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);        // holster_left
        hips.texOffs(28, 8).addBox(-4.5F, 11.0F, -2.8F, 1.0F, 2.0F, 1.0F, 0.0F, false);        // strap_left

        ballRight = new ModelRenderer(this);
        ballRight.setPos(0.0F, 0.0F, 0.0F);
        hips.addChild(ballRight);
        ballRight.texOffs(32, 8).addBox(3.0F, 11.0F, -4.8F, 2.0F, 2.0F, 2.0F, 0.0F, false);

        ballLeft = new ModelRenderer(this);
        ballLeft.setPos(0.0F, 0.0F, 0.0F);
        hips.addChild(ballLeft);
        ballLeft.texOffs(40, 8).addBox(-5.0F, 11.0F, -4.8F, 2.0F, 2.0F, 2.0F, 0.0F, false);

        // Same cubes with the Wrecking Ball's copper/orange skin (texture rows 16-19)
        wreckingRight = new ModelRenderer(this);
        wreckingRight.setPos(0.0F, 0.0F, 0.0F);
        hips.addChild(wreckingRight);
        wreckingRight.texOffs(32, 16).addBox(3.0F, 11.0F, -4.8F, 2.0F, 2.0F, 2.0F, 0.0F, false);

        wreckingLeft = new ModelRenderer(this);
        wreckingLeft.setPos(0.0F, 0.0F, 0.0F);
        hips.addChild(wreckingLeft);
        wreckingLeft.texOffs(40, 16).addBox(-5.0F, 11.0F, -4.8F, 2.0F, 2.0F, 2.0F, 0.0F, false);
    }

    /** Fits the belt to the wearer's current pose (called after the wearer's model setupAnim). */
    public void copyPose(BipedModel<?> wearer) {
        belt.copyFrom(wearer.body);
        float legY = (wearer.rightLeg.y + wearer.leftLeg.y) * 0.5F;
        float legZ = (wearer.rightLeg.z + wearer.leftLeg.z) * 0.5F;
        hips.setPos(0.0F, legY - LEG_Y, legZ - LEG_Z);
        hips.xRot = 0.0F;
        hips.yRot = wearer.body.yRot; // follows the torso turn of an arm swing
        hips.zRot = 0.0F;
    }

    /**
     * A ball is drawn in a holster only while the holster actually holds it, with the skin of the
     * ball kind stored there: first ball in the right holster, second in the left one.
     */
    public void setBalls(BallSkin right, BallSkin left) {
        ballRight.visible = right == BallSkin.STEEL;
        wreckingRight.visible = right == BallSkin.WRECKING;
        ballLeft.visible = left == BallSkin.STEEL;
        wreckingLeft.visible = left == BallSkin.WRECKING;
    }

    public enum BallSkin {
        NONE, STEEL, WRECKING
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay,
            float red, float green, float blue, float alpha) {
        belt.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        hips.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
