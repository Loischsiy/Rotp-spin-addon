package com.loischsiy.rotpspin.compat.curios;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.loischsiy.rotpspin.client.render.GyrosHolsterRender;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICurio;

/**
 * Curios view of a holster stack: makes it visible on the player in the belt slot.
 * {@link #render} is invoked by Curios on the client only; the client class is touched only there.
 */
class HolsterCurio implements ICurio {
    private final ItemStack holster;

    HolsterCurio(ItemStack holster) {
        this.holster = holster;
    }

    @Override
    public boolean canRender(String identifier, int index, LivingEntity livingEntity) {
        return true;
    }

    @Override
    public void render(String identifier, int index, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int light,
            LivingEntity livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
            float netHeadYaw, float headPitch) {
        GyrosHolsterRender.render(holster, matrixStack, renderTypeBuffer, light, livingEntity);
    }

    static class Provider implements ICapabilityProvider {
        private final LazyOptional<ICurio> curio;

        Provider(ItemStack holster) {
            HolsterCurio instance = new HolsterCurio(holster);
            this.curio = LazyOptional.of(() -> instance);
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return CuriosCapability.ITEM.orEmpty(cap, curio);
        }
    }
}
