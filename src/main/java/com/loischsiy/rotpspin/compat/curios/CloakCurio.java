package com.loischsiy.rotpspin.compat.curios;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.loischsiy.rotpspin.client.render.GyrosCloakRender;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICurio;

/**
 * Curios view of a cloak stack: makes it visible on the player in the back slot.
 * {@link #render} is invoked by Curios on the client only; the client class is touched only there.
 * The cloak keeps no state, so one shared instance serves every stack.
 */
class CloakCurio implements ICurio {
    private static final CloakCurio INSTANCE = new CloakCurio();

    @Override
    public boolean canRender(String identifier, int index, LivingEntity livingEntity) {
        return true;
    }

    @Override
    public void render(String identifier, int index, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int light,
            LivingEntity livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
            float netHeadYaw, float headPitch) {
        GyrosCloakRender.render(matrixStack, renderTypeBuffer, light, livingEntity);
    }

    static class Provider implements ICapabilityProvider {
        private final LazyOptional<ICurio> curio = LazyOptional.of(() -> INSTANCE);

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return CuriosCapability.ITEM.orEmpty(cap, curio);
        }
    }
}
