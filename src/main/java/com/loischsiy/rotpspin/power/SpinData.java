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

    public static Optional<SpinData> of(LivingEntity entity) {
        return INonStandPower.getNonStandPowerOptional(entity).resolve()
                .flatMap(power -> power.getTypeSpecificData(InitPowers.SPIN.get()));
    }

    public static int requiredLesson(Action<?> action) {
        if (action == InitPowers.SPIN_MUSCLE_HIJACK.get() || action == InitPowers.SPIN_HEALING.get()) {
            return 2;
        }
        if (action == InitPowers.SPIN_ITEM_THROW.get() || action == InitPowers.SPIN_BALL_STEER.get()) {
            return 3;
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

    private int effectiveLesson() {
        return SpinConfig.LESSONS_ENABLED.get() ? lesson : SpinLessons.MAX;
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
        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT nbt) {
        lesson = SpinLessons.clamp(nbt.contains("Lesson") ? nbt.getInt("Lesson") : SpinLessons.FIRST);
        ballHits = nbt.getInt("BallHits");
        hijacks = nbt.getInt("Hijacks");
        goldenHits = nbt.getInt("GoldenHits");
    }

    @Override
    public void syncWithUserOnly(ServerPlayerEntity user) {
        sync(user);
    }

    @Override
    public void syncWithTrackingOrUser(LivingEntity user, ServerPlayerEntity entity) {}
}
