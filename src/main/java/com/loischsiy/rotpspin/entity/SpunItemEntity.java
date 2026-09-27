package com.loischsiy.rotpspin.entity;

import com.github.standobyte.jojo.entity.itemprojectile.ItemNbtProjectileEntity;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.init.InitEntities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

/**
 * Lesson 3 "Believe in the rotation": any ordinary item thrown with Spin (a cork, a stone, a nail).
 * Only the perfect steel ball comes back, so this one flies like an arrow and is picked up where it lands.
 * The stack is synced to the client for rendering.
 */
public class SpunItemEntity extends ItemNbtProjectileEntity {
    private static final DataParameter<ItemStack> ITEM = EntityDataManager.defineId(SpunItemEntity.class, DataSerializers.ITEM_STACK);

    public SpunItemEntity(EntityType<? extends SpunItemEntity> type, World world) {
        super(type, world);
    }

    public SpunItemEntity(World world, LivingEntity thrower, ItemStack thrownStack) {
        super(InitEntities.SPUN_ITEM.get(), world, thrower, thrownStack);
        entityData.set(ITEM, thrownStack.copy());
        setBaseDamage(SpinConfig.ITEM_SPIN_DAMAGE.get());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ITEM, ItemStack.EMPTY);
    }

    public ItemStack getItem() {
        return entityData.get(ITEM);
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        entityData.set(ITEM, thrownStack.copy());
    }
}
