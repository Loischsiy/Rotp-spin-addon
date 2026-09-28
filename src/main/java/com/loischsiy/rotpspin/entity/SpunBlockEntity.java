package com.loischsiy.rotpspin.entity;

import java.util.Optional;

import com.github.standobyte.jojo.entity.itemprojectile.ItemNbtProjectileEntity;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.init.InitEntities;
import com.loischsiy.rotpspin.power.SpinData;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

/**
 * Lesson 3 "Believe in the rotation" for blocks: a block ripped out by SpinThrow or thrown
 * from the hand (sneak + right click) flies like a crude steel ball. No return (only the perfect
 * sphere comes back): the crude matter is spent on the first hit and drops as resources there.
 */
public class SpunBlockEntity extends ItemNbtProjectileEntity {
    private static final DataParameter<Optional<BlockState>> BLOCK =
            EntityDataManager.defineId(SpunBlockEntity.class, DataSerializers.BLOCK_STATE);

    public SpunBlockEntity(EntityType<? extends SpunBlockEntity> type, World world) {
        super(type, world);
    }

    public SpunBlockEntity(World world, LivingEntity thrower, BlockState state) {
        super(InitEntities.SPUN_BLOCK.get(), world, thrower, new ItemStack(state.getBlock()));
        entityData.set(BLOCK, Optional.of(state));
        setBaseDamage(SpinConfig.BLOCK_SPIN_DAMAGE.get());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(BLOCK, Optional.empty());
    }

    public BlockState getBlockState() {
        return entityData.get(BLOCK).orElse(Blocks.AIR.defaultBlockState());
    }

    @Override
    protected boolean hurtTarget(Entity target, Entity thrower) {
        boolean hurt = super.hurtTarget(target, thrower);
        if (hurt && !level.isClientSide() && thrower instanceof LivingEntity) {
            SpinData.practiceHit((LivingEntity) thrower, target);
        }
        return hurt;
    }

    @Override
    protected void changeMovementAfterHit() {
        // Crude matter is spent on the first hit: it drops as resources where it struck.
        if (!level.isClientSide()) {
            Block.dropResources(getBlockState(), level, blockPosition());
            remove();
        }
    }

    @Override
    protected void onHitBlock(BlockRayTraceResult result) {
        if (!level.isClientSide()) {
            BlockPos pos = result.getBlockPos().relative(result.getDirection());
            Block.dropResources(getBlockState(), level, pos);
            remove();
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Block")) {
            entityData.set(BLOCK, Optional.of(NBTUtil.readBlockState(compound.getCompound("Block"))));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        compound.put("Block", NBTUtil.writeBlockState(getBlockState()));
    }
}
