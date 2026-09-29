package com.loischsiy.rotpspin.entity;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.entity.itemprojectile.ItemNbtProjectileEntity;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.holster.IHolsterAccess;
import com.loischsiy.rotpspin.init.InitEffects;
import com.loischsiy.rotpspin.init.InitEntities;
import com.loischsiy.rotpspin.init.InitItems;
import com.loischsiy.rotpspin.item.GyrosHolsterItem;
import com.loischsiy.rotpspin.item.SteelBallItem;
import com.loischsiy.rotpspin.item.WreckingBallItem;
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
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
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
    // Wrecking Ball (royal guard version): hidden satellites and a shockwave on a miss
    private boolean wrecking;
    private boolean satellite;
    private boolean satellitesReleased;
    private boolean shockwaveDone;

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
        this.wrecking = thrownStack.getItem() instanceof WreckingBallItem;
    }

    /** Marks this ball as a satellite: it strikes once and is spent (no return, no steering). */
    void setSatellite() {
        this.satellite = true;
    }

    /** Lesson 4 Golden Spin / lesson 5 Super Spin bonus of the thrower, 1.0 if not learned or not calibrated. */
    private static double goldenMultiplier(World world, LivingEntity thrower, boolean chipped) {
        double mult = SpinData.goldenMultiplier(world, thrower);
        return chipped ? SpinGolden.applyChipped(mult, SpinConfig.GOLDEN_CHIPPED_RETENTION.get()) : mult;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SPINNING, false);
        entityData.define(RETURNING, false);
    }

    @Override
    public void tick() {
        if (satellite && (inGround || tickCount > SpinConfig.WRECKING_SATELLITE_LIFETIME.get())) {
            if (!level.isClientSide()) {
                remove();
            }
            return;
        }
        if (!level.isClientSide() && wrecking && !satellite && isSpinning() && !isReturning() && !satellitesReleased
                && tickCount - steeredTicks >= SpinConfig.WRECKING_RELEASE_AFTER_TICKS.get()) {
            satellitesReleased = true;
            releaseSatellites();
        }

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
        return isAlive() && isSpinning() && !isReturning() && !inGround && !satellite;
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
        if (satellite) {
            // A satellite strikes once and is spent.
            if (!level.isClientSide()) {
                remove();
            }
            return;
        }
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
        if (wrecking && !satellite && isSpinning() && !isReturning() && !shockwaveDone) {
            // Even a miss raises a shockwave (docs/spin-lore.md): once per flight.
            shockwaveDone = true;
            if (!level.isClientSide()) {
                emitShockwave();
            }
        }
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
        if (hurt && !level.isClientSide() && isSpinning() && !satellite && thrower instanceof LivingEntity) {
            SpinData.practiceHit((LivingEntity) thrower, target);
        }
        return hurt;
    }

    /** Server side: the satellites hidden inside the sphere fly out at the nearest victim. */
    private void releaseSatellites() {
        Entity owner = getOwner();
        if (!(owner instanceof LivingEntity)) {
            return;
        }
        double range = SpinConfig.WRECKING_SATELLITE_RANGE.get();
        LivingEntity victim = null;
        double closestDistSq = Double.MAX_VALUE;
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(range),
                e -> e.isAlive() && !e.getUUID().equals(owner.getUUID()))) {
            double distSq = candidate.distanceToSqr(this);
            if (distSq < closestDistSq) {
                closestDistSq = distSq;
                victim = candidate;
            }
        }
        Vector3d aimDir;
        if (victim != null) {
            aimDir = new Vector3d(victim.getX() - getX(),
                    victim.getY() + victim.getBbHeight() * 0.5 - getY(), victim.getZ() - getZ());
        }
        else {
            aimDir = getDeltaMovement();
        }
        if (aimDir.lengthSqr() < 1e-6) {
            aimDir = getLookAngle();
        }
        double speed = SpinConfig.WRECKING_SATELLITE_SPEED.get();
        double damage = SpinConfig.WRECKING_SATELLITE_DAMAGE.get();
        for (Vector3d velocity : WreckingBall.satelliteVelocities(aimDir,
                SpinConfig.WRECKING_SATELLITES.get(), speed)) {
            SteelBallEntity sat = new SteelBallEntity(level, (LivingEntity) owner,
                    new ItemStack(InitItems.WRECKING_BALL.get()), isSpinning());
            sat.setSatellite();
            sat.setBaseDamage(damage);
            sat.pickup = AbstractArrowEntity.PickupStatus.DISALLOWED;
            sat.setPos(getX(), getY(), getZ());
            sat.shoot(velocity.x, velocity.y, velocity.z, (float) velocity.length(), 0.0F);
            sat.hurtMarked = true;
            level.addFreshEntity(sat);
        }
        playSound(SoundEvents.TRIDENT_THROW, 0.6F, 1.6F);
    }

    /** Server side: a miss still wounds and disorients (hemispatial neglect) around the impact. */
    private void emitShockwave() {
        Entity owner = getOwner();
        double radius = SpinConfig.WRECKING_SHOCKWAVE_RADIUS.get();
        float damage = SpinConfig.WRECKING_SHOCKWAVE_DAMAGE.get().floatValue();
        int neglectTicks = SpinConfig.WRECKING_NEGLECT_DURATION.get();
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius),
                e -> e.isAlive() && (owner == null || !e.getUUID().equals(owner.getUUID())))) {
            victim.hurt(DamageSource.thrown(this, owner == null ? this : owner), damage);
            victim.addEffect(new EffectInstance(InitEffects.NEGLECT.get(), neglectTicks));
            Vector3d away = new Vector3d(victim.getX() - getX(), 0, victim.getZ() - getZ());
            if (away.lengthSqr() > 1e-6) {
                away = away.normalize();
                victim.push(away.x * 0.6, 0.25, away.z * 0.6);
            }
        }
        playSound(SoundEvents.GENERIC_EXPLODE, 0.5F, 1.5F);
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

    /** Royal guard version: the renderer draws the brass band. */
    public boolean isWrecking() {
        return wrecking;
    }

    /** A spent satellite: the renderer draws it smaller. */
    public boolean isSatellite() {
        return satellite;
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
        wrecking = compound.getBoolean("Wrecking");
        satellite = compound.getBoolean("Satellite");
        satellitesReleased = compound.getBoolean("SatellitesReleased");
        shockwaveDone = compound.getBoolean("ShockwaveDone");
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Spinning", isSpinning());
        compound.putBoolean("Returning", isReturning());
        compound.putInt("ReturnTicks", returnTicks);
        compound.putInt("SteeredTicks", steeredTicks);
        compound.putInt("Bounces", bounces);
        compound.putBoolean("Wrecking", wrecking);
        compound.putBoolean("Satellite", satellite);
        compound.putBoolean("SatellitesReleased", satellitesReleased);
        compound.putBoolean("ShockwaveDone", shockwaveDone);
    }
}
