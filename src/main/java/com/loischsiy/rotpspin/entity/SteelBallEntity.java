package com.loischsiy.rotpspin.entity;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.entity.itemprojectile.ItemNbtProjectileEntity;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.holster.IHolsterAccess;
import com.loischsiy.rotpspin.init.InitEntities;
import com.loischsiy.rotpspin.init.InitItems;
import com.loischsiy.rotpspin.item.GyrosHolsterItem;
import com.loischsiy.rotpspin.item.SteelBallItem;
import com.loischsiy.rotpspin.power.SpinData;
import com.loischsiy.rotpspin.power.SpinGolden;
import com.loischsiy.rotpspin.power.SpinSteer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

/**
 * Thrown steel ball. With Spin it returns to the thrower (Zeppeli signature technique);
 * without Spin it behaves like a plain thrown object and has to be picked up.
 * Base: RotP {@code ItemNbtProjectileEntity} (as {@code BladeHatEntity}); homing return
 * follows vanilla {@code TridentEntity} loyalty.
 */
public class SteelBallEntity extends ItemNbtProjectileEntity {
    private static final DataParameter<Boolean> SPINNING = EntityDataManager.defineId(SteelBallEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> RETURNING = EntityDataManager.defineId(SteelBallEntity.class, DataSerializers.BOOLEAN);

    private int returnTicks;
    // Ticks of flight spent under the thrower's control; they do not count towards the return timer.
    private int steeredTicks;
    // Ricochets off blocks during this flight
    private int bounces;

    public SteelBallEntity(EntityType<? extends SteelBallEntity> type, World world) {
        super(type, world);
    }

    public SteelBallEntity(World world, LivingEntity thrower, ItemStack thrownStack, boolean spinning) {
        super(InitEntities.STEEL_BALL.get(), world, thrower, thrownStack);
        setSpinning(spinning);
        double damage = spinning ? SpinConfig.BALL_SPIN_DAMAGE.get() : SpinConfig.BALL_PLAIN_DAMAGE.get();
        boolean chipped = SteelBallItem.isChipped(thrownStack);
        if (chipped) {
            damage *= SpinConfig.BALL_DAMAGED_MULTIPLIER.get();
        }
        if (spinning) {
            damage *= goldenMultiplier(world, thrower, chipped);
        }
        setBaseDamage(damage);
    }

    /** Lesson 4 Golden Spin / lesson 5 Super Spin bonus of the thrower, 1.0 if not learned or not calibrated. */
    private static double goldenMultiplier(World world, LivingEntity thrower, boolean chipped) {
        int lesson = SpinData.of(thrower).map(data -> data.getLesson()).orElse(0);
        if (lesson < 4) {
            return 1.0;
        }
        String category = world.getBiome(thrower.blockPosition()).getBiomeCategory().name();
        boolean buckle = thrower instanceof PlayerEntity && hasBuckle((PlayerEntity) thrower);
        boolean calibrated = SpinGolden.isCalibrated(lesson, category, buckle, SpinConfig.GOLDEN_DEAD_CATEGORIES.get());
        double mult = SpinGolden.multiplier(lesson, calibrated,
                SpinConfig.GOLDEN_MULT_4.get(), SpinConfig.GOLDEN_MULT_5.get());
        return chipped ? SpinGolden.applyChipped(mult, SpinConfig.GOLDEN_CHIPPED_RETENTION.get()) : mult;
    }

    private static boolean hasBuckle(PlayerEntity player) {
        for (int i = 0; i < player.inventory.getContainerSize(); i++) {
            if (player.inventory.getItem(i).getItem() == InitItems.CALIBRATION_BUCKLE.get()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SPINNING, false);
        entityData.define(RETURNING, false);
    }

    @Override
    public void tick() {
        if (!level.isClientSide() && isSpinning() && !isReturning()
                && (tickCount - steeredTicks >= SpinConfig.BALL_RETURN_AFTER_TICKS.get() || inGround)) {
            startReturning();
        }

        if (isReturning()) {
            Entity owner = getOwner();
            if (!level.isClientSide() && (!isAcceptableReturnOwner(owner)
                    || ++returnTicks > SpinConfig.BALL_MAX_RETURN_TICKS.get())) {
                loseSpin();
            }
            else if (owner != null) {
                setNoPhysics(true);
                Vector3d toOwner = new Vector3d(owner.getX() - getX(), owner.getEyeY() - getY(), owner.getZ() - getZ());
                double acceleration = SpinConfig.BALL_RETURN_ACCELERATION.get();
                setDeltaMovement(getDeltaMovement().scale(0.95D).add(toOwner.normalize().scale(acceleration)));
            }
        }

        super.tick();
    }

    /** A ball that the thrower can still steer: spinning, flying forward, not stuck in a block. */
    public boolean canBeSteered() {
        return isAlive() && isSpinning() && !isReturning() && !inGround;
    }

    /**
     * Server side, once per tick of the "Steel Ball Control" ability: re-aims the flight towards
     * {@code aimPoint} and postpones the return while the ball is held.
     */
    public void steerTowards(Vector3d aimPoint) {
        if (level.isClientSide() || !canBeSteered()) {
            return;
        }
        Vector3d desired = aimPoint.subtract(position());
        setDeltaMovement(SpinSteer.steer(getDeltaMovement(), desired,
                SpinConfig.STEER_TURN_RATE.get(), SpinConfig.STEER_MIN_SPEED.get()));
        steeredTicks++;
        // Forces a velocity packet this tick (TrackedEntity#sendChanges): the entity type updates only every 20 ticks.
        hurtMarked = true;
    }

    /** The user's own steerable ball closest to them within {@code range}, or null. Works on both sides. */
    @Nullable
    public static SteelBallEntity findSteerable(LivingEntity user, double range) {
        List<SteelBallEntity> balls = user.level.getEntitiesOfClass(SteelBallEntity.class,
                user.getBoundingBox().inflate(range),
                ball -> ball.canBeSteered() && ball.getOwner() != null && ball.getOwner().getUUID().equals(user.getUUID()));
        SteelBallEntity closest = null;
        double closestDistSq = Double.MAX_VALUE;
        for (SteelBallEntity ball : balls) {
            double distSq = ball.distanceToSqr(user);
            if (distSq < closestDistSq) {
                closest = ball;
                closestDistSq = distSq;
            }
        }
        return closest;
    }

    private void startReturning() {
        inGround = false;
        returnTicks = 0;
        setNoPhysics(true);
        setReturning(true);
    }

    private void loseSpin() {
        setReturning(false);
        setSpinning(false);
        setNoPhysics(false);
    }

    private static boolean isAcceptableReturnOwner(Entity owner) {
        if (owner == null || !owner.isAlive()) {
            return false;
        }
        return !(owner instanceof PlayerEntity) || !owner.isSpectator();
    }

    @Override
    protected void changeMovementAfterHit() {
        // Hit an entity: a spinning ball comes back instead of bouncing off.
        if (isSpinning()) {
            if (!level.isClientSide() && !isReturning()) {
                startReturning();
            }
        }
        else {
            super.changeMovementAfterHit();
        }
    }

    /**
     * A spinning ball ricochets off block faces (the return relies on rotation and ricochets,
     * docs/spin-lore.md) a limited number of times; then it lands as usual and starts returning.
     * Runs on both sides, the server also forces a velocity packet.
     */
    @Override
    protected void onHitBlock(BlockRayTraceResult result) {
        if (isSpinning() && !isReturning()) {
            Vector3d normal = Vector3d.atLowerCornerOf(result.getDirection().getNormal());
            Vector3d reflected = SpinRicochet.reflect(getDeltaMovement(), normal, SpinConfig.BALL_RICOCHET_SPEED_RETENTION.get());
            if (SpinRicochet.canBounce(bounces, SpinConfig.BALL_RICOCHET_MAX_BOUNCES.get(),
                    reflected.length(), SpinConfig.BALL_RICOCHET_MIN_SPEED.get())) {
                bounces++;
                setDeltaMovement(reflected);
                playSound(SoundEvents.ANVIL_LAND, 0.25F, 1.8F);
                if (!level.isClientSide()) {
                    hurtMarked = true;
                }
                return;
            }
        }
        super.onHitBlock(result);
    }

    // A hit on a creature with a spinning ball is practice for lesson 2.
    @Override
    protected boolean hurtTarget(Entity target, Entity thrower) {
        boolean hurt = super.hurtTarget(target, thrower);
        if (hurt && !level.isClientSide() && isSpinning() && thrower instanceof LivingEntity) {
            SpinData.practiceHit((LivingEntity) thrower, target);
        }
        return hurt;
    }

    @Override
    protected void onHit(RayTraceResult rayTraceResult) {
        super.onHit(rayTraceResult);
        if (!level.isClientSide() && rayTraceResult.getType() == RayTraceResult.Type.BLOCK
                && random.nextDouble() < SpinConfig.BALL_DAMAGED_CHANCE_ON_BLOCK_HIT.get()) {
            SteelBallItem.setChipped(thrownStack, true);
        }
    }

    // The returning ball goes back into the main hand if it is free, otherwise into a holster with room.
    @Override
    public void playerTouch(PlayerEntity player) {
        if (level.isClientSide() || !isReturning() || getOwner() == null || !getOwner().getUUID().equals(player.getUUID())) {
            super.playerTouch(player);
            return;
        }
        if (pickup == AbstractArrowEntity.PickupStatus.ALLOWED) {
            ItemStack ball = getPickupItem();
            ItemStack holster = player.getMainHandItem().isEmpty() ? ItemStack.EMPTY
                    : IHolsterAccess.current().findHolster(player, GyrosHolsterItem::hasSpace);
            if (player.getMainHandItem().isEmpty()) {
                player.setItemInHand(Hand.MAIN_HAND, ball);
            }
            else if (!holster.isEmpty() && GyrosHolsterItem.insertBall(holster, ball)) {
                // back in the holster
            }
            else if (!player.inventory.add(ball)) {
                spawnAtLocation(ball);
            }
        }
        pickUp(player);
    }

    public boolean isSpinning() {
        return entityData.get(SPINNING);
    }

    private void setSpinning(boolean spinning) {
        entityData.set(SPINNING, spinning);
    }

    public boolean isReturning() {
        return entityData.get(RETURNING);
    }

    private void setReturning(boolean returning) {
        entityData.set(RETURNING, returning);
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        setSpinning(compound.getBoolean("Spinning"));
        setReturning(compound.getBoolean("Returning"));
        returnTicks = compound.getInt("ReturnTicks");
        steeredTicks = compound.getInt("SteeredTicks");
        bounces = compound.getInt("Bounces");
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Spinning", isSpinning());
        compound.putBoolean("Returning", isReturning());
        compound.putInt("ReturnTicks", returnTicks);
        compound.putInt("SteeredTicks", steeredTicks);
        compound.putInt("Bounces", bounces);
    }
}
