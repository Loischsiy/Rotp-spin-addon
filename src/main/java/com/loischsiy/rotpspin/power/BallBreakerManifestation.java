package com.loischsiy.rotpspin.power;

import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.init.InitItems;
import com.loischsiy.rotpspin.init.InitStands;
import com.loischsiy.rotpspin.item.SteelBallItem;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

/**
 * Ball Breaker manifests in an experienced master who performs the highest throwing technique
 * of the Zeppeli family: a Golden Spin ball thrown on a galloping horse (SBR ch. 83).
 * An imperfect sphere cannot take Super Spin (ch. 84), so a chipped ball does not manifest it.
 * The detour of lesson 5 (ch. 85) is Johnny's path to Tusk ACT4, not Ball Breaker.
 */
public final class BallBreakerManifestation {
    private BallBreakerManifestation() {}

    /** Pure rule of the manifestation. */
    public static boolean canManifest(boolean enabled, int lesson, int minLesson, boolean gallopSuperSpin,
            boolean perfectBall, boolean spinning, boolean hasStand) {
        return enabled && spinning && lesson >= minLesson && gallopSuperSpin && perfectBall && !hasStand;
    }

    /**
     * Imperfect sphere (SBR ch. 84): with a chipped steel ball in the master's hand the touch of
     * senescence keeps only {@code chippedRetention} of its damage and duration.
     */
    public static double senescenceScale(boolean chippedBallInHand, double chippedRetention) {
        return chippedBallInHand ? chippedRetention : 1.0;
    }

    /** The user holds a chipped steel ball (main or off hand). */
    public static boolean holdsChippedBall(net.minecraft.entity.LivingEntity user) {
        for (ItemStack stack : user.getHandSlots()) {
            if (stack.getItem() == InitItems.STEEL_BALL.get() && SteelBallItem.isChipped(stack)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Server: called after a spinning throw of {@code ballStack}. Gives Ball Breaker to the thrower
     * (who must not already have a Stand) and, if configured, summons it at once. Returns true on manifestation.
     */
    public static boolean tryManifest(PlayerEntity player, ItemStack ballStack, boolean spinning) {
        if (player.level.isClientSide()) {
            return false;
        }
        IStandPower stand = IStandPower.getStandPowerOptional(player).resolve().orElse(null);
        if (stand == null) {
            return false;
        }
        boolean perfectBall = ballStack.getItem() == InitItems.STEEL_BALL.get() && !SteelBallItem.isChipped(ballStack);
        boolean gallop = SpinData.of(player).map(SpinData::hasGallopSuperSpin).orElse(false);
        if (!canManifest(SpinConfig.BALL_BREAKER_MANIFEST_ENABLED.get(), SpinData.lessonOf(player),
                SpinConfig.BALL_BREAKER_MANIFEST_MIN_LESSON.get(), gallop, perfectBall, spinning, stand.hasPower())) {
            return false;
        }
        if (!stand.givePower(InitStands.STAND_BALL_BREAKER.getStandType())) {
            return false;
        }
        if (SpinConfig.BALL_BREAKER_MANIFEST_SUMMON.get()) {
            InitStands.STAND_BALL_BREAKER.getStandType().summon(player, stand, false);
        }
        player.displayClientMessage(new TranslationTextComponent("rotp_spin.message.ball_breaker_manifested")
                .withStyle(TextFormatting.GOLD), true);
        player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundCategory.PLAYERS, 1.0F, 1.4F);
        if (player.level instanceof ServerWorld) {
            ((ServerWorld) player.level).sendParticles(ParticleTypes.CRIT,
                    player.getX(), player.getY(1.0), player.getZ(), 40, 0.6, 0.8, 0.6, 0.2);
        }
        return true;
    }
}
