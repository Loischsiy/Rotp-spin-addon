package com.loischsiy.rotpspin.power;

import java.util.Optional;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.TypeSpecificData;
import com.loischsiy.rotpspin.client.ClientSpinState;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.init.InitPowers;
import com.loischsiy.rotpspin.network.AddonPackets;
import com.loischsiy.rotpspin.network.s2c.SpinLessonSyncPacket;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Spin-specific power data: Gyro's lesson progress (see {@link SpinLessons}). Spin energy lives in
 * SpinPowerCapability. Techniques of a later lesson stay locked in RotP's hotbar
 * ({@link #isActionUnlocked}) until the lesson is learned by practice or set with /spinlesson.
 */
public class SpinData extends TypeSpecificData {
    private int lesson = SpinLessons.FIRST;
    private int ballHits;
    private int hijacks;
    private int goldenHits;
    /** Game time until which the golden rectangle framed by hands calibrates Golden Spin. */
    private long handFrameUntil;
    // Super Spin, not saved: a relog is a stop, the gallop starts over.
    private int gallopTicks;
    private int slowTicks;
    private double horseSpeed;
    private boolean hasLastHorsePos;
    private double lastHorseX;
    private double lastHorseZ;
    private long detourUntil;

    public static Optional<SpinData> of(LivingEntity entity) {
        return INonStandPower.getNonStandPowerOptional(entity).resolve()
                .flatMap(power -> power.getTypeSpecificData(InitPowers.SPIN.get()));
    }

    /** Current lesson of the Spin user, 0 without the Spin power. */
    public static int lessonOf(LivingEntity entity) {
        return of(entity).map(SpinData::getLesson).orElse(0);
    }

    /**
     * Server: Golden Spin / Super Spin damage multiplier of the thrower (1.0 if not learned
     * or not calibrated). Lesson 5 works everywhere; lesson 4 needs a living biome or the
     * calibration buckle — or a horse at full gallop (the detour of lesson 5, usable from lesson 4).
     */
    public static double goldenMultiplier(net.minecraft.world.World world, LivingEntity thrower) {
        return goldenMultiplier(world, thrower, false);
    }

    /**
     * Same, for a ball that may be chipped: an imperfect sphere cannot take Super Spin (SBR ch. 84)
     * and falls back to the ordinary Golden Spin of the lesson.
     */
    public static double goldenMultiplier(net.minecraft.world.World world, LivingEntity thrower, boolean chipped) {
        int lesson = lessonOf(thrower);
        if (lesson < 4) {
            return 1.0;
        }
        double base = calibratedMultiplier(world, thrower, lesson);
        boolean superSpin = !chipped && of(thrower).map(data -> data.hasSuperSpin(world.getGameTime())).orElse(false);
        return SpinSuperSpin.multiplier(superSpin, base, SpinConfig.SUPER_SPIN_MULT.get());
    }

    private static double calibratedMultiplier(net.minecraft.world.World world, LivingEntity thrower, int lesson) {
        String category = world.getBiome(thrower.blockPosition()).getBiomeCategory().name();
        boolean buckle = thrower instanceof net.minecraft.entity.player.PlayerEntity
                && hasBuckle((net.minecraft.entity.player.PlayerEntity) thrower);
        boolean snowfall = SpinConfig.GOLDEN_SNOWFALL_CALIBRATES.get() && isSnowingOn(world, thrower);
        boolean handFrame = of(thrower).map(data -> data.hasHandFrame(world.getGameTime())).orElse(false);
        boolean calibrated = SpinGolden.isCalibrated(lesson, category, buckle, snowfall || handFrame,
                SpinConfig.GOLDEN_DEAD_CATEGORIES.get());
        return SpinGolden.multiplier(lesson, calibrated,
                SpinConfig.GOLDEN_MULT_4.get(), SpinConfig.GOLDEN_MULT_5.get());
    }

    /** Vanilla snow line: below this biome temperature precipitation falls as snow. */
    private static final float SNOW_TEMPERATURE = 0.15F;

    /**
     * Snow is falling on the thrower: the snowy counterpart of vanilla World#isRainingAt
     * (weather on, open sky above the head, snowy biome cold enough at this height).
     */
    static boolean isSnowingOn(net.minecraft.world.World world, LivingEntity thrower) {
        if (!world.isRaining()) {
            return false;
        }
        net.minecraft.util.math.BlockPos head = thrower.blockPosition().above();
        if (!world.canSeeSky(head)) {
            return false;
        }
        net.minecraft.world.biome.Biome biome = world.getBiome(head);
        return biome.getPrecipitation() == net.minecraft.world.biome.Biome.RainType.SNOW
                && biome.getTemperature(head) < SNOW_TEMPERATURE;
    }

    /**
     * Server, every tick of the rider: the natural gallop of a healthy horse builds Super Spin
     * (from lesson 4); a crash or a hit on the horse or the rider breaks it completely, while brief
     * slow-downs on rough ground are tolerated (SpinSuperSpin).
     * Speed is measured from the horse's position between ticks: a player-ridden horse is moved
     * by the client, so its server-side motion vector stays near zero.
     */
    public void tickHorseback(LivingEntity rider) {
        net.minecraft.entity.Entity vehicle = rider.getVehicle();
        boolean wasReady = isGallopReady();
        if (lesson < 4 || !(vehicle instanceof net.minecraft.entity.passive.horse.AbstractHorseEntity)) {
            gallopTicks = 0;
            slowTicks = 0;
            horseSpeed = 0;
            hasLastHorsePos = false;
        } else {
            net.minecraft.entity.passive.horse.AbstractHorseEntity horse =
                    (net.minecraft.entity.passive.horse.AbstractHorseEntity) vehicle;
            double raw = 0.0;
            if (hasLastHorsePos) {
                double dx = horse.getX() - lastHorseX;
                double dz = horse.getZ() - lastHorseZ;
                raw = Math.sqrt(dx * dx + dz * dz);
            }
            lastHorseX = horse.getX();
            lastHorseZ = horse.getZ();
            hasLastHorsePos = true;
            boolean crash = SpinSuperSpin.isCrash(horse.horizontalCollision, raw, horseSpeed,
                    SpinConfig.SUPER_SPIN_CRASH_STOP_FRACTION.get());
            horseSpeed = SpinSuperSpin.smoothSpeed(horseSpeed, raw);
            boolean galloping = SpinGolden.isGallopSuperSpin(lesson, horseSpeed, SpinConfig.GOLDEN_HORSE_GALLOP_SPEED.get());
            slowTicks = SpinSuperSpin.nextSlowTicks(slowTicks, galloping);
            boolean broken = crash || isAttacked(horse) || isAttacked(rider);
            gallopTicks = SpinSuperSpin.nextGallopTicks(gallopTicks, galloping, slowTicks,
                    SpinConfig.SUPER_SPIN_GRACE_TICKS.get(), broken, isHealthy(horse));
        }
        boolean ready = isGallopReady();
        if (ready != wasReady) {
            serverPlayer.ifPresent(player -> player.displayClientMessage(new TranslationTextComponent(
                    ready ? "rotp_spin.message.super_spin_ready" : "rotp_spin.message.super_spin_broken")
                    .withStyle(ready ? TextFormatting.GOLD : TextFormatting.GRAY), true));
        }
    }

    /**
     * A blow that disturbs the gallop. A landing after a jump off a hill (fall damage, which the horse
     * also passes to the rider) is part of riding over rough ground, not an attack.
     */
    private static boolean isAttacked(LivingEntity entity) {
        if (entity.hurtTime <= 0) {
            return false;
        }
        net.minecraft.util.DamageSource last = entity.getLastDamageSource();
        return last != net.minecraft.util.DamageSource.FALL;
    }

    private boolean isGallopReady() {
        return SpinSuperSpin.isGallopReady(gallopTicks, SpinConfig.SUPER_SPIN_GALLOP_TICKS.get());
    }

    /** Super Spin from the natural gallop of the horse (not the detour kick) is in the user now. */
    public boolean hasGallopSuperSpin() {
        return lesson >= 4 && isGallopReady();
    }

    /** Super Spin from the gallop or from the detour kick is in the user now. */
    public boolean hasSuperSpin(long gameTime) {
        return (lesson >= 4 && isGallopReady()) || gameTime < detourUntil;
    }

    static boolean isHealthy(net.minecraft.entity.passive.horse.AbstractHorseEntity horse) {
        return SpinSuperSpin.isHealthy(horse.getHealth(), horse.getMaxHealth(),
                SpinConfig.SUPER_SPIN_HORSE_MIN_HEALTH.get());
    }

    /**
     * Server: the lesson 5 detour (SBR ch. 85). A spinning ball of the user hits the leg of the
     * user's own horse: the muscles are hijacked (lesson 2), the horse kicks the user and the kick
     * hands over Super Spin. Returns false (ordinary hit) when the detour does not apply.
     */
    public static boolean tryDetour(LivingEntity thrower, net.minecraft.entity.passive.horse.AbstractHorseEntity horse,
            boolean chipped) {
        if (!SpinConfig.SUPER_SPIN_DETOUR_ENABLED.get()) {
            return false;
        }
        boolean own = thrower.getVehicle() == horse
                || (horse.isTamed() && thrower.getUUID().equals(horse.getOwnerUUID()));
        if (!SpinSuperSpin.canDetour(lessonOf(thrower), chipped, own, isHealthy(horse),
                horse.distanceToSqr(thrower), SpinConfig.SUPER_SPIN_DETOUR_RANGE.get())) {
            return false;
        }
        Optional<SpinData> data = of(thrower);
        if (!data.isPresent()) {
            return false;
        }
        long now = thrower.level.getGameTime();
        int duration = SpinConfig.SUPER_SPIN_DETOUR_DURATION_TICKS.get();
        data.get().detourUntil = SpinSuperSpin.detourUntil(now, duration);
        horse.makeMad();
        horse.playSound(SoundEvents.HORSE_ANGRY, 1.0F, 1.0F);
        float kick = SpinConfig.SUPER_SPIN_DETOUR_KICK_DAMAGE.get().floatValue();
        if (kick > 0) {
            thrower.hurt(net.minecraft.util.DamageSource.mobAttack(horse), kick);
        }
        data.get().serverPlayer.ifPresent(player -> player.displayClientMessage(new TranslationTextComponent(
                "rotp_spin.message.super_spin_detour", duration / 20).withStyle(TextFormatting.GOLD), true));
        return true;
    }

    static boolean hasBuckle(net.minecraft.entity.player.PlayerEntity player) {
        for (int i = 0; i < player.inventory.getContainerSize(); i++) {
            if (player.inventory.getItem(i).getItem() == com.loischsiy.rotpspin.init.InitItems.CALIBRATION_BUCKLE.get()) {
                return true;
            }
        }
        return false;
    }

    public static int requiredLesson(Action<?> action) {
        if (action == InitPowers.SPIN_MUSCLE_HIJACK.get() || action == InitPowers.SPIN_HEALING.get()) {
            return 2;
        }
        if (action == InitPowers.SPIN_ITEM_THROW.get() || action == InitPowers.SPIN_BALL_STEER.get()
                || action == InitPowers.SPIN_BLOCK_THROW.get()) {
            return 3;
        }
        if (action == InitPowers.SPIN_GOLDEN_FRAME.get()) {
            return 4;
        }
        return 1;
    }

    @Override
    public boolean isActionUnlocked(Action<INonStandPower> action, INonStandPower powerData) {
        LivingEntity user = powerData.getUser();
        // COMMON config is not synced: the client uses the lesson in effect sent by the server.
        int inEffect = user != null && user.level.isClientSide() ? ClientSpinState.getLesson() : effectiveLesson();
        return requiredLesson(action) <= inEffect;
    }

    public int getLesson() {
        return lesson;
    }

    public int getBallHits() {
        return ballHits;
    }

    public int getHijacks() {
        return hijacks;
    }

    public int getGoldenHits() {
        return goldenHits;
    }

    /** Server: the golden rectangle is framed by hands now and stays in the eye for {@code durationTicks}. */
    public void frameGoldenRectangle(long gameTime, int durationTicks) {
        handFrameUntil = SpinGolden.handFrameUntil(gameTime, durationTicks);
    }

    public boolean hasHandFrame(long gameTime) {
        return SpinGolden.isHandFrameActive(gameTime, handFrameUntil);
    }

    private int effectiveLesson() {
        return SpinConfig.LESSONS_ENABLED.get() ? lesson : SpinLessons.MAX;
    }

    /** Server: a spinning projectile of this user hit a creature (practice for lessons 2-5). */
    public static void practiceHit(LivingEntity thrower, net.minecraft.entity.Entity target) {
        if (target instanceof LivingEntity
                && !(target instanceof net.minecraft.entity.item.ArmorStandEntity)
                && !(target instanceof com.github.standobyte.jojo.entity.stand.StandEntity)) {
            of(thrower).ifPresent(data -> {
                data.onSpinBallHit();
                data.onGoldenHit();
            });
        }
    }

    /** Server: a spinning steel ball of this user hit a creature (practice for lesson 2). */
    public void onSpinBallHit() {
        if (lesson == 1) {
            ballHits++;
            showProgress(2, ballHits, SpinConfig.LESSON2_BALL_HITS.get());
            checkProgress();
        }
    }

    /** Server: a successful Muscle Hijack (practice for lesson 3). */
    public void onMuscleHijack() {
        if (lesson == 2) {
            hijacks++;
            showProgress(3, hijacks, SpinConfig.LESSON3_HIJACKS.get());
            checkProgress();
        }
    }

    /** Server: a spinning steel ball of this user hit a creature at lesson 3+ (practice for lessons 4-5). */
    public void onGoldenHit() {
        if (lesson >= 3 && lesson < SpinLessons.MAX) {
            goldenHits++;
            showProgress(lesson + 1, goldenHits, lesson == 3
                    ? SpinConfig.LESSON4_GOLDEN_HITS.get() : SpinConfig.LESSON5_GOLDEN_HITS.get());
            checkProgress();
        }
    }

    private void checkProgress() {
        int reached = SpinLessons.reached(lesson, ballHits, hijacks, goldenHits,
                SpinConfig.LESSON2_BALL_HITS.get(), SpinConfig.LESSON3_HIJACKS.get(),
                SpinConfig.LESSON4_GOLDEN_HITS.get(), SpinConfig.LESSON5_GOLDEN_HITS.get());
        if (reached > lesson) {
            setLesson(reached, true);
        }
    }

    /** Server: sets the lesson (command or learning); practice counters of lower lessons are kept. */
    public void setLesson(int newLesson, boolean announce) {
        int old = lesson;
        lesson = SpinLessons.clamp(newLesson);
        if (lesson < old) {
            // Taken back: the practice has to be repeated.
            ballHits = 0;
            hijacks = 0;
            goldenHits = 0;
        }
        serverPlayer.ifPresent(player -> {
            sync(player);
            if (announce && lesson > old) {
                player.sendMessage(new TranslationTextComponent("rotp_spin.message.lesson_learned",
                        lesson, new TranslationTextComponent("rotp_spin.lesson." + lesson))
                        .withStyle(TextFormatting.GOLD), Util.NIL_UUID);
                player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
        });
    }

    private void showProgress(int nextLesson, int done, int required) {
        if (!SpinConfig.LESSONS_ENABLED.get()) {
            return;
        }
        serverPlayer.ifPresent(player -> player.displayClientMessage(new TranslationTextComponent(
                "rotp_spin.message.lesson_progress", nextLesson, Math.min(done, required), required)
                .withStyle(TextFormatting.YELLOW), true));
    }

    private void sync(ServerPlayerEntity player) {
        AddonPackets.sendToClient(new SpinLessonSyncPacket(effectiveLesson()), player);
    }

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("Lesson", lesson);
        nbt.putInt("BallHits", ballHits);
        nbt.putInt("Hijacks", hijacks);
        nbt.putInt("GoldenHits", goldenHits);
        nbt.putLong("HandFrameUntil", handFrameUntil);
        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT nbt) {
        lesson = SpinLessons.clamp(nbt.contains("Lesson") ? nbt.getInt("Lesson") : SpinLessons.FIRST);
        ballHits = nbt.getInt("BallHits");
        hijacks = nbt.getInt("Hijacks");
        goldenHits = nbt.getInt("GoldenHits");
        handFrameUntil = nbt.getLong("HandFrameUntil");
    }

    @Override
    public void syncWithUserOnly(ServerPlayerEntity user) {
        sync(user);
    }

    @Override
    public void syncWithTrackingOrUser(LivingEntity user, ServerPlayerEntity entity) {}
}
