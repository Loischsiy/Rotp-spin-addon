package com.loischsiy.rotpspin.entity;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.entity.itemprojectile.ItemNbtProjectileEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.holster.IHolsterAccess;
import com.loischsiy.rotpspin.init.InitEffects;
import com.loischsiy.rotpspin.init.InitEntities;
import com.loischsiy.rotpspin.init.InitItems;
import com.loischsiy.rotpspin.item.GyrosHolsterItem;
import com.loischsiy.rotpspin.item.SteelBallItem;
import com.loischsiy.rotpspin.item.WreckingBallItem;
import com.loischsiy.rotpspin.power.SpinData;
import com.loischsiy.rotpspin.power.BallBreakerAging;
import com.loischsiy.rotpspin.power.BallBreakerBoost;
import com.loischsiy.rotpspin.power.BallBreakerManifestation;
import com.loischsiy.rotpspin.power.SpinGolden;
import com.loischsiy.rotpspin.power.SpinResonance;
import com.loischsiy.rotpspin.power.SpinSqueeze;
import com.loischsiy.rotpspin.power.SpinSteer;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.DrownedEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.potion.Effects;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/**
 * Thrown steel ball. With Spin it returns to the thrower (Zeppeli signature technique);
 * without Spin it behaves like a plain thrown object and has to be picked up.
 * Base: RotP {@code ItemNbtProjectileEntity} (as {@code BladeHatEntity}); homing return
 * follows vanilla {@code TridentEntity} loyalty.
 */
public class SteelBallEntity extends ItemNbtProjectileEntity {
    private static final DataParameter<Boolean> SPINNING = EntityDataManager.defineId(SteelBallEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> RETURNING = EntityDataManager.defineId(SteelBallEntity.class, DataSerializers.BOOLEAN);
    // Wrecking Ball: satellites have flown out (client draws empty sockets instead of gold balls)
    private static final DataParameter<Boolean> SATELLITES_RELEASED = EntityDataManager.defineId(SteelBallEntity.class, DataSerializers.BOOLEAN);
    // Royal guard body (client draws the copper sphere with gold satellites) and
    // spent satellite ball (client draws a small gold sphere, not a steel ball).
    // Plain fields stay server-side only, so both flags ride the EntityDataManager.
    private static final DataParameter<Boolean> WRECKING = EntityDataManager.defineId(SteelBallEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SATELLITE = EntityDataManager.defineId(SteelBallEntity.class, DataSerializers.BOOLEAN);
    // Rope (SBR ch. 55): ROPE_NONE, ROPE_FLYING (sneak throw), ROPE_ANCHORED (stuck in a block, pulling the thrower).
    private static final DataParameter<Byte> ROPE = EntityDataManager.defineId(SteelBallEntity.class, DataSerializers.BYTE);
    private static final byte ROPE_NONE = 0;
    private static final byte ROPE_FLYING = 1;
    private static final byte ROPE_ANCHORED = 2;
    // Visual only: hemp-coloured dust along the rope.
    private static final RedstoneParticleData ROPE_DUST = new RedstoneParticleData(0.76F, 0.64F, 0.43F, 0.6F);
    // Visual only: polished-gold dust (#ebc731) trailing a satellite and bursting at release.
    private static final RedstoneParticleData GOLD_DUST = new RedstoneParticleData(0.92F, 0.78F, 0.19F, 1.0F);

    private int returnTicks;
    // Ticks of flight spent under the thrower's control; they do not count towards the return timer.
    private int steeredTicks;
    // Ricochets off blocks during this flight
    private int bounces;
    // Wrecking Ball (royal guard version): hidden satellites and a shockwave on a miss
    private boolean satellitesReleased;
    private boolean shockwaveDone;
    private boolean bulletsCut;
    // Ticks the anchored rope has been pulling the thrower.
    private int ropeTicks;
    // Where the ball was thrown from: a holster throw returns to the holster, a hand throw to the hand.
    private boolean fromHolster;
    // Spin resonance: extra ticks of forward flight granted by spinning projectiles near the throw.
    private int resonanceTicks;
    // Server, visual only: resonating sources counted at the throw (not saved, a reload just drops the trail).
    private int resonanceSources;
    /** Server, visual only: the launch ring has been emitted (the first tick may already see tickCount 1). */
    private boolean resonanceRung;

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
            if (!world.isClientSide() && SpinConfig.RESONANCE_ENABLED.get()) {
                int sources = countResonanceSources(world, thrower, SpinConfig.RESONANCE_RADIUS.get());
                int maxSources = SpinConfig.RESONANCE_MAX_SOURCES.get();
                damage *= SpinResonance.damageMultiplier(sources, maxSources,
                        SpinConfig.RESONANCE_DAMAGE_PER_SOURCE.get(), SpinConfig.RESONANCE_MAX_MULTIPLIER.get());
                resonanceTicks = SpinResonance.extraFlightTicks(sources, maxSources,
                        SpinConfig.RESONANCE_EXTRA_FLIGHT_TICKS_PER_SOURCE.get());
                resonanceSources = SpinResonance.effectiveSources(sources, maxSources);
            }
        }
        setBaseDamage(damage);
        setWrecking(thrownStack.getItem() instanceof WreckingBallItem);
    }

    /**
     * A Wrecking Ball satellite: fixed damage, always spinning, no Golden/resonance lookups
     * (the release spawns many at once and would repeat the same entity searches for each).
     */
    private SteelBallEntity(World world, LivingEntity owner, double satelliteDamage) {
        super(InitEntities.STEEL_BALL.get(), world, owner, new ItemStack(InitItems.WRECKING_BALL.get()));
        setSpinning(true);
        setWrecking(true);
        setSatellite();
        setBaseDamage(satelliteDamage);
    }

    /**
     * Server: spinning projectiles near the thrower that resonate with a new throw (SBR ch. 23):
     * other spinning steel balls (satellites excluded), spun items and spun blocks, of any owner.
     */
    private static int countResonanceSources(World world, LivingEntity thrower, double radius) {
        if (thrower == null || radius <= 0.0) {
            return 0;
        }
        net.minecraft.util.math.AxisAlignedBB area = thrower.getBoundingBox().inflate(radius);
        int sources = world.getEntitiesOfClass(SteelBallEntity.class, area,
                ball -> ball.isAlive() && ball.isSpinning() && !ball.isSatellite()).size();
        // A spun item that landed lies still (stuck like an arrow): it no longer carries rotation.
        sources += world.getEntitiesOfClass(SpunItemEntity.class, area,
                e -> e.isAlive() && e.getDeltaMovement().lengthSqr() > 1.0E-4).size();
        sources += world.getEntitiesOfClass(SpunBlockEntity.class, area, Entity::isAlive).size();
        return sources;
    }

    /**
     * Server, visual only: a resonating throw rings out at launch (gold dust ring + bell resonance)
     * and leaves a glowing trail while it flies forward, denser with more sources.
     */
    private void emitResonanceParticles() {
        ServerWorld world = (ServerWorld) level;
        if (!resonanceRung) {
            resonanceRung = true;
            int points = 12 * resonanceSources;
            for (int i = 0; i < points; i++) {
                double angle = Math.PI * 2.0 * i / points;
                world.sendParticles(GOLD_DUST, getX() + Math.cos(angle) * 0.6, getY(), getZ() + Math.sin(angle) * 0.6,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
            playSound(SoundEvents.BELL_RESONATE, 0.5F, 1.6F);
        }
        if (isSpinning() && !isReturning() && !inGround) {
            world.sendParticles(net.minecraft.particles.ParticleTypes.END_ROD, xo, yo, zo,
                    resonanceSources, 0.05, 0.05, 0.05, 0.0);
        }
        else {
            // The ball turned back or landed: the resonance has been spent.
            resonanceSources = 0;
        }
    }

    /** Marks this ball as a satellite: it strikes once and is spent (no return, no steering). */
    void setSatellite() {
        entityData.set(SATELLITE, true);
    }

    private void setWrecking(boolean wrecking) {
        entityData.set(WRECKING, wrecking);
    }

    /** Marks this ball as thrown straight from the holster: it returns to the holster, not to the hand. */
    public void setFromHolster(boolean fromHolster) {
        this.fromHolster = fromHolster;
    }

    /** Lesson 4 Golden Spin / lesson 5 Super Spin bonus of the thrower, 1.0 if not learned or not calibrated.
     * A summoned Ball Breaker amplifies the Spin itself, but only on top of Golden (SBR ch. 83). */
    private static double goldenMultiplier(World world, LivingEntity thrower, boolean chipped) {
        double mult = SpinData.goldenMultiplier(world, thrower, chipped);
        if (chipped) {
            mult = SpinGolden.applyChipped(mult, SpinConfig.GOLDEN_CHIPPED_RETENTION.get());
        }
        return BallBreakerBoost.boostedMultiplier(mult,
                BallBreakerBoost.hasBallBreakerOut(thrower), SpinConfig.BALL_BREAKER_SPIN_DAMAGE_MULT.get());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SPINNING, false);
        entityData.define(RETURNING, false);
        entityData.define(SATELLITES_RELEASED, false);
        entityData.define(WRECKING, false);
        entityData.define(SATELLITE, false);
        entityData.define(ROPE, ROPE_NONE);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        super.writeSpawnData(buffer);
        // The tracker syncs EntityDataManager only every updateInterval (20 ticks):
        // a 60-tick satellite would fly a third of its life as a steel ball without this.
        buffer.writeBoolean(isWrecking());
        buffer.writeBoolean(isSatellite());
        buffer.writeByte(getRope());
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        super.readSpawnData(additionalData);
        setWrecking(additionalData.readBoolean());
        if (additionalData.readBoolean()) {
            setSatellite();
        }
        entityData.set(ROPE, additionalData.readByte());
    }

    @Override
    public void tick() {
        if (isSatellite() && (inGround || tickCount > SpinConfig.WRECKING_SATELLITE_LIFETIME.get())) {
            if (!level.isClientSide()) {
                remove();
            }
            return;
        }
        if (isSatellite() && level.isClientSide()) {
            // A 0.25-block ball at bullet speed is lost against the sky: a gold trail shows its path.
            level.addParticle(GOLD_DUST, xo, yo, zo, 0.0, 0.0, 0.0);
        }
        if (resonanceSources > 0 && !level.isClientSide()) {
            emitResonanceParticles();
        }
        if (tickCount - steeredTicks >= SpinConfig.WRECKING_RELEASE_AFTER_TICKS.get()) {
            releaseSatellitesOnce(null);
        }

        if (getRope() == ROPE_ANCHORED && !level.isClientSide()) {
            tickRope();
        }

        // A satellite is spent matter: it never returns to the thrower. An anchored rope holds the ball in place.
        if (!level.isClientSide() && isSpinning() && !isReturning() && !isSatellite() && getRope() != ROPE_ANCHORED
                && (tickCount - steeredTicks >= SpinConfig.BALL_RETURN_AFTER_TICKS.get() + resonanceTicks || inGround)) {
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
        return isAlive() && isSpinning() && !isReturning() && !inGround && !isSatellite();
    }

    /** A ball of the thrower that lost its rotation (parry, missed return, plain throw): lies waiting for a re-spin. */
    public boolean canBeRecalled() {
        return isAlive() && !isSatellite() && !isSpinning() && !isReturning();
    }

    /** Server side: re-spin a dropped ball and send it back to the thrower (as the initial throw, same cost). */
    public void respinAndReturn() {
        setSpinning(true);
        startReturning();
        hurtMarked = true;
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

    /** The user's own dropped (de-spun) ball closest to them within {@code range}, or null. Works on both sides. */
    @Nullable
    public static SteelBallEntity findRecallable(LivingEntity user, double range) {
        List<SteelBallEntity> balls = user.level.getEntitiesOfClass(SteelBallEntity.class,
                user.getBoundingBox().inflate(range),
                ball -> ball.canBeRecalled() && ball.getOwner() != null && ball.getOwner().getUUID().equals(user.getUUID()));
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

    /** Server side, before the throw: a sneak throw weaves a rope (SBR ch. 55). */
    public void makeRope() {
        entityData.set(ROPE, ROPE_FLYING);
    }

    private byte getRope() {
        return entityData.get(ROPE);
    }

    /** Server side: the rope ball hit a block. Anchors within the rope length, otherwise it is just a stuck ball. */
    private void anchorRope() {
        Entity owner = getOwner();
        if (owner != null && owner.level == level
                && SpinRope.canAnchor(distanceTo(owner), SpinConfig.ROPE_MAX_LENGTH.get())) {
            entityData.set(ROPE, ROPE_ANCHORED);
            ropeTicks = 0;
            playSound(SoundEvents.TRIPWIRE_ATTACH, 1.0F, 0.8F);
        }
        else {
            entityData.set(ROPE, ROPE_NONE);
        }
    }

    /** Server side, every tick while anchored: the spin reels the thrower in, then the ball comes back. */
    private void tickRope() {
        Entity owner = getOwner();
        if (owner == null || !owner.isAlive() || owner.level != level || !inGround || !isSpinning()) {
            releaseRope();
            return;
        }
        Vector3d toAnchor = position().subtract(owner.position().add(0.0D, owner.getBbHeight() * 0.5D, 0.0D));
        if (SpinRope.shouldRelease(toAnchor.length(), ++ropeTicks,
                SpinConfig.ROPE_RELEASE_DISTANCE.get(), SpinConfig.ROPE_MAX_TICKS.get())) {
            releaseRope();
            return;
        }
        owner.setDeltaMovement(SpinRope.pull(owner.getDeltaMovement(), toAnchor,
                SpinConfig.ROPE_PULL_STRENGTH.get(), SpinConfig.ROPE_MAX_SPEED.get()));
        // Players move client-side: hurtMarked sends them the velocity packet this tick.
        owner.hurtMarked = true;
        owner.fallDistance = 0.0F;
        if (ropeTicks % 2 == 0) {
            emitRopeParticles(owner);
        }
    }

    private void releaseRope() {
        entityData.set(ROPE, ROPE_NONE);
        if (isSpinning()) {
            startReturning();
        }
    }

    private void emitRopeParticles(Entity owner) {
        Vector3d from = owner.position().add(0.0D, owner.getBbHeight() * 0.6D, 0.0D);
        Vector3d span = position().subtract(from);
        int points = Math.min(24, Math.max(2, (int) (span.length() * 1.5D)));
        ServerWorld world = (ServerWorld) level;
        for (int i = 1; i < points; i++) {
            Vector3d p = from.add(span.scale((double) i / points));
            world.sendParticles(ROPE_DUST, p.x, p.y, p.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
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
        if (isSatellite()) {
            // A satellite strikes once and is spent.
            if (!level.isClientSide()) {
                remove();
            }
            return;
        }
        // Hit an entity: a spinning ball comes back instead of bouncing off.
        if (isSpinning()) {
            if (!level.isClientSide() && !isReturning()) {
                // A close target is struck before the release timer: the satellites burst out on impact.
                releaseSatellitesOnce(null);
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
        if (getRope() == ROPE_FLYING && isSpinning() && !isReturning()) {
            // The rope ball sticks where it lands: no ricochet, no friction tricks.
            if (!level.isClientSide()) {
                anchorRope();
            }
            super.onHitBlock(result);
            return;
        }
        if (isWrecking() && !isSatellite() && isSpinning() && !isReturning() && !shockwaveDone) {
            // Even a miss raises a shockwave (docs/spin-lore.md): once per flight.
            shockwaveDone = true;
            if (!level.isClientSide()) {
                emitShockwave();
            }
        }
        if (!level.isClientSide()) {
            releaseSatellitesOnce(result.getLocation());
            stripBark(result);
            cutBullets(result);
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

    /** Server: friction of the rotation strips the bark off a log, like an axe (SBR ch. 30). */
    private void stripBark(BlockRayTraceResult result) {
        if (!SpinFriction.canStripBark(SpinConfig.FRICTION_BARK_STRIPPING.get(), isSpinning(), isReturning(),
                getDeltaMovement().length(), SpinConfig.FRICTION_BARK_MIN_SPEED.get())) {
            return;
        }
        Entity owner = getOwner();
        net.minecraft.util.math.BlockPos pos = result.getBlockPos();
        if (!(owner instanceof PlayerEntity) || !level.mayInteract((PlayerEntity) owner, pos)
                || !((PlayerEntity) owner).mayUseItemAt(pos, result.getDirection(), ItemStack.EMPTY)) {
            return;
        }
        net.minecraft.block.BlockState stripped = net.minecraft.item.AxeItem.getAxeStrippingState(level.getBlockState(pos));
        if (stripped == null) {
            return;
        }
        level.setBlock(pos, stripped, 11);
        level.playSound(null, pos, SoundEvents.AXE_STRIP, net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);
        setDeltaMovement(getDeltaMovement().scale(
                SpinFriction.speedAfterStrip(1.0, SpinConfig.FRICTION_BARK_SPEED_RETENTION.get())));
        hurtMarked = true;
    }

    /**
     * Server: the rotation cuts bullets out of an iron block (SBR ch. 44): iron nuggets fly off
     * the face, and now and then the carved block is used up (see {@link SpinFriction#blockConsumeChance}).
     */
    private void cutBullets(BlockRayTraceResult result) {
        if (!SpinFriction.canCutBullets(SpinConfig.FRICTION_BULLET_CUTTING.get(), isSpinning(), isReturning(),
                bulletsCut, getDeltaMovement().length(), SpinConfig.FRICTION_BULLET_MIN_SPEED.get())) {
            return;
        }
        net.minecraft.util.math.BlockPos pos = result.getBlockPos();
        if (!level.getBlockState(pos).is(net.minecraft.block.Blocks.IRON_BLOCK)) {
            return;
        }
        Entity owner = getOwner();
        if (!(owner instanceof PlayerEntity) || !level.mayInteract((PlayerEntity) owner, pos)
                || !((PlayerEntity) owner).mayUseItemAt(pos, result.getDirection(), ItemStack.EMPTY)) {
            return;
        }
        bulletsCut = true;
        int bullets = SpinConfig.FRICTION_BULLETS_PER_CUT.get();
        Vector3d face = result.getLocation().add(Vector3d.atLowerCornerOf(result.getDirection().getNormal()).scale(0.25));
        net.minecraft.entity.item.ItemEntity drop = new net.minecraft.entity.item.ItemEntity(level,
                face.x, face.y, face.z, new ItemStack(net.minecraft.item.Items.IRON_NUGGET, bullets));
        drop.setDefaultPickUpDelay();
        level.addFreshEntity(drop);
        if (random.nextDouble() < SpinFriction.blockConsumeChance(bullets, SpinConfig.FRICTION_BULLET_NUGGETS_PER_BLOCK.get())) {
            level.destroyBlock(pos, false, owner);
        }
        level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, net.minecraft.util.SoundCategory.BLOCKS, 0.8F, 1.6F);
        setDeltaMovement(getDeltaMovement().scale(
                SpinFriction.speedAfterStrip(1.0, SpinConfig.FRICTION_BULLET_SPEED_RETENTION.get())));
        hurtMarked = true;
    }

    // A hit on a creature with a spinning ball is practice for lesson 2.
    // A Golden throw under the summoned Ball Breaker also ages the victim (senescence, SBR ch. 83-84).
    @Override
    protected boolean hurtTarget(Entity target, Entity thrower) {
        // Lesson 5 detour: a spinning ball on the leg of your own horse makes it kick you (SBR ch. 85).
        if (!level.isClientSide() && isSpinning() && !isSatellite() && thrower instanceof LivingEntity
                && target instanceof AbstractHorseEntity
                && SpinData.tryDetour((LivingEntity) thrower, (AbstractHorseEntity) target,
                        SteelBallItem.isChipped(thrownStack))) {
            return true;
        }
        // Counter-rotation: a Super Spin ball unwinds Tusk's infinite rotation (SBR ch. 86-87, optional rotp_t).
        if (!level.isClientSide() && isSpinning() && !isSatellite() && thrower instanceof LivingEntity
                && target instanceof LivingEntity) {
            AddonMain.getTuskCompat().onSpinBallHit((LivingEntity) target, (LivingEntity) thrower,
                    SteelBallItem.isChipped(thrownStack));
        }
        boolean hurt = super.hurtTarget(target, thrower);
        if (hurt && !level.isClientSide() && isSpinning() && !isSatellite() && thrower instanceof LivingEntity) {
            SpinData.practiceHit((LivingEntity) thrower, target);
            if (target instanceof LivingEntity
                    && BallBreakerBoost.shouldBoost(BallBreakerBoost.hasBallBreakerOut((LivingEntity) thrower),
                            SpinData.goldenMultiplier(level, (LivingEntity) thrower))) {
                BallBreakerAging.apply((LivingEntity) target, BallBreakerManifestation.senescenceScale(
                        SteelBallItem.isChipped(thrownStack), SpinConfig.BALL_BREAKER_CHIPPED_RETENTION.get()));
            }
            if (target instanceof LivingEntity && target.isAlive()) {
                squeeze((LivingEntity) target, (LivingEntity) thrower);
            }
        }
        return hurt;
    }

    /**
     * The spinning ball flattens the limb it hits and wrings water out of the body
     * (docs/spin-lore.md, SBR ch. 2/20). The gameplay form is an assumption (⚠️ in the lore doc).
     */
    private void squeeze(LivingEntity target, LivingEntity thrower) {
        if (!SpinSqueeze.applies(SpinConfig.SQUEEZE_ENABLED.get(), SpinData.lessonOf(thrower),
                SpinConfig.SQUEEZE_MIN_LESSON.get())) {
            return;
        }
        boolean chipped = SteelBallItem.isChipped(thrownStack);
        double damagedMult = SpinConfig.BALL_DAMAGED_MULTIPLIER.get();
        double hitY = getY() + getBbHeight() * 0.5;
        SpinSqueeze.Zone zone = SpinSqueeze.zone(
                SpinSqueeze.relativeHeight(hitY, target.getY(), target.getBbHeight()),
                SpinConfig.SQUEEZE_LEG_HEIGHT.get(), SpinConfig.SQUEEZE_HEAD_HEIGHT.get());
        int limbTicks = SpinSqueeze.duration(SpinConfig.SQUEEZE_LIMB_TICKS.get(), chipped, damagedMult);
        int amplifier = SpinConfig.SQUEEZE_LIMB_AMPLIFIER.get();
        if (limbTicks > 0 && zone == SpinSqueeze.Zone.LEGS) {
            target.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, limbTicks, amplifier));
        } else if (limbTicks > 0 && zone == SpinSqueeze.Zone.BODY) {
            target.addEffect(new EffectInstance(Effects.WEAKNESS, limbTicks, amplifier));
        }
        int dryTicks = SpinSqueeze.duration(SpinConfig.SQUEEZE_DRY_TICKS.get(), chipped, damagedMult);
        if (dryTicks <= 0) {
            return;
        }
        target.addEffect(new EffectInstance(InitEffects.DESICCATION.get(), dryTicks));
        target.clearFire();
        if (level instanceof ServerWorld) {
            ((ServerWorld) level).sendParticles(ParticleTypes.DRIPPING_WATER, target.getX(), hitY, target.getZ(),
                    12, target.getBbWidth() * 0.4, 0.2, target.getBbWidth() * 0.4, 0.0);
        }
        float bonus = SpinConfig.SQUEEZE_WATER_MOB_BONUS_DAMAGE.get().floatValue();
        if (bonus > 0 && (target.getMobType() == CreatureAttribute.WATER || target instanceof DrownedEntity)) {
            // The ball's own hit just set hurt immunity; the wrung-out water is a separate wound.
            target.invulnerableTime = 0;
            target.hurt(DamageSource.indirectMagic(this, thrower), bonus);
        }
    }

    /**
     * A parry by another Stand knocks the rotation out of the ball: it loses Spin and drops
     * where it was hit instead of coming back. The thrower's own Stand lets the ball fly
     * (as Silver Chariot spares its user's projectiles).
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof StandEntity) {
            StandEntity stand = (StandEntity) source.getDirectEntity();
            LivingEntity standUser = stand.getUser();
            Entity owner = getOwner();
            if (shouldDropOnStandParry(isSpinning() || isReturning() || isSatellite(),
                    owner == null ? null : owner.getUUID(),
                    standUser == null ? null : standUser.getUUID())) {
                if (isSatellite()) {
                    // A satellite is spent matter with no pickup: a parry just swats it away.
                    if (!level.isClientSide()) {
                        remove();
                    }
                    return true;
                }
                loseSpin();
                setDeltaMovement(Vector3d.ZERO);
                if (!level.isClientSide()) {
                    // The entity type updates every 20 ticks: force a velocity packet this tick.
                    hurtMarked = true;
                }
                playSound(SoundEvents.ANVIL_LAND, 0.25F, 1.8F);
                return true;
            }
        }
        return super.hurt(source, amount);
    }

    /**
     * Pure rule for a Stand parry: an active ball (flying, returning, or a satellite)
     * drops when hit by a Stand that is not its thrower's own. No World access.
     */
    static boolean shouldDropOnStandParry(boolean ballActive, UUID ownerUuid, UUID standUserUuid) {
        if (!ballActive) {
            return false;
        }
        return ownerUuid == null || standUserUuid == null || !ownerUuid.equals(standUserUuid);
    }

    /**
     * Server side: the satellites fly out once per flight of a spinning Wrecking Ball — on the
     * release timer or on the first impact, whichever comes first (without the impact trigger a
     * target closer than the timer distance was hit before anything flew out).
     * {@code impactPoint}: block hit location, satellites start just off the surface; null — from the centre.
     */
    private void releaseSatellitesOnce(@Nullable Vector3d impactPoint) {
        if (level.isClientSide() || !isWrecking() || isSatellite() || !isSpinning() || isReturning()
                || satellitesReleased) {
            return;
        }
        satellitesReleased = true;
        entityData.set(SATELLITES_RELEASED, true);
        Vector3d from = position();
        if (impactPoint != null) {
            // Step back along the flight so the satellites do not spawn inside the block.
            Vector3d back = getDeltaMovement().lengthSqr() > 1e-6 ? getDeltaMovement().normalize().scale(-0.3) : Vector3d.ZERO;
            from = impactPoint.add(back);
        }
        releaseSatellites(from);
        ((ServerWorld) level).sendParticles(GOLD_DUST, from.x, from.y, from.z, 24, 0.25, 0.25, 0.25, 0.0);
    }

    /** Server side: the satellites hidden inside the sphere fly out at the nearest victim. */
    private void releaseSatellites(Vector3d from) {
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
            SteelBallEntity sat = new SteelBallEntity(level, (LivingEntity) owner, damage);
            sat.pickup = AbstractArrowEntity.PickupStatus.DISALLOWED;
            sat.setPos(from.x, from.y, from.z);
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

    // The returning ball goes back where it was thrown from: a holster throw into a holster with room,
    // a hand throw into the free main hand. Each falls back to the other place, then to the inventory.
    @Override
    public void playerTouch(PlayerEntity player) {
        if (level.isClientSide() || !isReturning() || getOwner() == null || !getOwner().getUUID().equals(player.getUUID())) {
            super.playerTouch(player);
            return;
        }
        if (pickup == AbstractArrowEntity.PickupStatus.ALLOWED) {
            ItemStack ball = getPickupItem();
            ItemStack holster = IHolsterAccess.current().findHolster(player, GyrosHolsterItem::hasSpace);
            if (fromHolster) {
                if (!holster.isEmpty() && GyrosHolsterItem.insertBall(holster, ball)) {
                    // back in the holster
                }
                else if (player.getMainHandItem().isEmpty()) {
                    player.setItemInHand(Hand.MAIN_HAND, ball);
                }
                else if (!player.inventory.add(ball)) {
                    spawnAtLocation(ball);
                }
            }
            else if (player.getMainHandItem().isEmpty()) {
                player.setItemInHand(Hand.MAIN_HAND, ball);
            }
            else if (!holster.isEmpty() && GyrosHolsterItem.insertBall(holster, ball)) {
                // hand was busy: back in the holster
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

    /** Royal guard version: the renderer draws the copper body with gold satellites. */
    public boolean isWrecking() {
        return entityData.get(WRECKING);
    }

    /** A spent satellite: the renderer draws it as a small gold ball. */
    public boolean isSatellite() {
        return entityData.get(SATELLITE);
    }

    /** Wrecking Ball whose satellites have already flown out (synced to the client). */
    public boolean areSatellitesReleased() {
        return entityData.get(SATELLITES_RELEASED);
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
        setWrecking(compound.getBoolean("Wrecking"));
        if (compound.getBoolean("Satellite")) {
            setSatellite();
        }
        satellitesReleased = compound.getBoolean("SatellitesReleased");
        entityData.set(SATELLITES_RELEASED, satellitesReleased);
        shockwaveDone = compound.getBoolean("ShockwaveDone");
        bulletsCut = compound.getBoolean("BulletsCut");
        entityData.set(ROPE, compound.getByte("Rope"));
        ropeTicks = compound.getInt("RopeTicks");
        fromHolster = compound.getBoolean("FromHolster");
        resonanceTicks = compound.getInt("ResonanceTicks");
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Spinning", isSpinning());
        compound.putBoolean("Returning", isReturning());
        compound.putInt("ReturnTicks", returnTicks);
        compound.putInt("SteeredTicks", steeredTicks);
        compound.putInt("Bounces", bounces);
        compound.putBoolean("Wrecking", isWrecking());
        compound.putBoolean("Satellite", isSatellite());
        compound.putBoolean("SatellitesReleased", satellitesReleased);
        compound.putBoolean("ShockwaveDone", shockwaveDone);
        compound.putBoolean("BulletsCut", bulletsCut);
        compound.putByte("Rope", getRope());
        compound.putInt("RopeTicks", ropeTicks);
        compound.putBoolean("FromHolster", fromHolster);
        compound.putInt("ResonanceTicks", resonanceTicks);
    }
}
