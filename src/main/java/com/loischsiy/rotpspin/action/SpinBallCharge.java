package com.loischsiy.rotpspin.action;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.non_stand.NonStandAction;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.loischsiy.rotpspin.client.ClientSpinState;
import com.loischsiy.rotpspin.client.anim.SpinPlayerAnimations;
import com.loischsiy.rotpspin.capability.SpinPower;
import com.loischsiy.rotpspin.capability.SpinPowerCapability;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.init.InitItems;
import com.loischsiy.rotpspin.item.SteelBallItem;
import com.loischsiy.rotpspin.power.SpinCharge;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/**
 * Ability (hold, lesson 1): wind the rotation of an ordinary steel ball up in the hand, the ball
 * is thrown on release. The longer the charge, the faster and harder the throw (Spin raises a
 * projectile's destructive power, docs/spin-lore.md, ch. 9). The price is Spin energy per tick and a
 * slowed, concentrated body. A chipped ball takes only part of the charge (an imperfect sphere
 * fails the higher rotation, ch. 84). The Wrecking Ball is a different weapon and cannot be charged.
 * Charging in the hand before the throw is a gameplay assumption (the manga shows the throw itself).
 */
public class SpinBallCharge extends NonStandAction {
    private static final String NBT_CHARGE = "rotp_spin.BallChargeTicks";
    // Presentation only (sound / HUD / particle rhythm), not gameplay numbers.
    private static final int SOUND_INTERVAL = 4;
    private static final int MESSAGE_INTERVAL = 5;
    private static final int RING_POINTS = 16;
    private static final RedstoneParticleData GOLD_DUST = new RedstoneParticleData(0.92F, 0.78F, 0.19F, 1.0F);
    private static final RedstoneParticleData GOLD_DUST_SMALL = new RedstoneParticleData(0.92F, 0.78F, 0.19F, 0.6F);

    public SpinBallCharge(NonStandAction.Builder builder) {
        super(builder);
    }

    private static int maxTicks() {
        return SpinConfig.CHARGE_MAX_TICKS.get();
    }

    private static double chippedMax() {
        return SpinConfig.CHARGE_CHIPPED_MAX.get();
    }

    /** Energy per tick is paid in {@link #holdTick} so the throw cost stays reserved. */
    @Override
    public float getHeldTickEnergyCost(INonStandPower power) {
        return 0;
    }

    /** The cooldown grows with how long the rotation was wound up. */
    @Override
    protected int getCooldownAdditional(INonStandPower power, int ticksHeld) {
        if (power.isUserCreative()) {
            return 0;
        }
        return (int) Math.round(SpinConfig.CHARGE_COOLDOWN_TICKS.get() * SpinCharge.fraction(ticksHeld, maxTicks()));
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!SpinConfig.CHARGE_ENABLED.get() || !(user instanceof PlayerEntity)) {
            return ActionConditionResult.NEGATIVE;
        }
        PlayerEntity player = (PlayerEntity) user;
        if (handWithPlainBall(player) == null) {
            boolean otherBall = player.getMainHandItem().getItem() instanceof SteelBallItem
                    || player.getOffhandItem().getItem() instanceof SteelBallItem;
            return conditionMessage(otherBall ? "rotp_spin.only_plain_steel_ball" : "rotp_spin.no_steel_ball_in_hand");
        }
        if (player.getCooldowns().isOnCooldown(InitItems.STEEL_BALL.get())) {
            return ActionConditionResult.NEGATIVE;
        }
        if (!player.abilities.instabuild && power.getEnergy() < SpinConfig.BALL_SPIN_COST.get()) {
            return conditionMessage("no_energy_spin");
        }
        return ActionConditionResult.POSITIVE;
    }

    @Override
    public void startedHolding(World world, LivingEntity user, INonStandPower power, ActionTarget target, boolean requirementsFulfilled) {
        user.getPersistentData().remove(NBT_CHARGE);
    }

    @Override
    protected void holdTick(World world, LivingEntity user, INonStandPower power, int ticksHeld,
            ActionTarget target, boolean requirementsFulfilled) {
        if (world.isClientSide() || !requirementsFulfilled || !(user instanceof PlayerEntity)) {
            return;
        }
        PlayerEntity player = (PlayerEntity) user;
        Hand hand = handWithPlainBall(player);
        if (hand == null) {
            return;
        }
        boolean chipped = SteelBallItem.isChipped(player.getItemInHand(hand));
        CompoundNBT data = player.getPersistentData();
        int charge = data.getInt(NBT_CHARGE);
        boolean wasFull = SpinCharge.isFull(charge, maxTicks(), chipped, chippedMax());
        if (!wasFull && payTick(player)) {
            charge++;
            data.putInt(NBT_CHARGE, charge);
        }
        boolean full = SpinCharge.isFull(charge, maxTicks(), chipped, chippedMax());
        double fraction = SpinCharge.cap(SpinCharge.fraction(charge, maxTicks()), chipped, chippedMax());

        int slowness = SpinConfig.CHARGE_SLOWNESS_AMPLIFIER.get();
        if (slowness >= 0) {
            player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 5, slowness, false, false, false));
        }

        ServerWorld serverWorld = (ServerWorld) world;
        if (!full && ticksHeld % SOUND_INTERVAL == 0) {
            // Rising whirr of the winding rotation.
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GRINDSTONE_USE, SoundCategory.PLAYERS, 0.35F, 0.6F + (float) fraction);
        }
        if (full && !wasFull) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BELL_RESONATE, SoundCategory.PLAYERS, 0.6F, 1.6F);
            double y = player.getY() + 0.1;
            for (int i = 0; i < RING_POINTS; i++) {
                double a = i * 2 * Math.PI / RING_POINTS;
                serverWorld.sendParticles(GOLD_DUST, player.getX() + Math.cos(a) * 0.9, y,
                        player.getZ() + Math.sin(a) * 0.9, 1, 0, 0, 0, 0);
            }
            serverWorld.sendParticles(ParticleTypes.ENCHANTED_HIT,
                    player.getX(), player.getY(0.6), player.getZ(), 20, 0.4, 0.4, 0.4, 0.2);
        }
        if (ticksHeld % MESSAGE_INTERVAL == 0 || (full && !wasFull)) {
            player.displayClientMessage(new TranslationTextComponent("rotp_spin.message.ball_charge",
                    SpinCharge.percent(fraction)).withStyle(full ? TextFormatting.GOLD : TextFormatting.YELLOW), true);
        }
    }

    /** Pays one tick of charging, keeping the energy of the spinning throw itself in reserve. */
    private static boolean payTick(PlayerEntity player) {
        if (player.abilities.instabuild) {
            return true;
        }
        SpinPower spin = SpinPowerCapability.get(player).orElse(null);
        if (spin == null) {
            return false;
        }
        float perTick = SpinConfig.CHARGE_ENERGY_PER_TICK.get().floatValue();
        float reserve = SpinConfig.BALL_SPIN_COST.get().floatValue();
        return spin.getEnergy() - perTick >= reserve && spin.tryConsume(perTick);
    }

    /** Release: throw the ball with the stored charge. */
    @Override
    public void stoppedHolding(World world, LivingEntity user, INonStandPower power, int ticksHeld, boolean willFire) {
        if (world.isClientSide() || !(user instanceof PlayerEntity)) {
            return;
        }
        PlayerEntity player = (PlayerEntity) user;
        int charge = player.getPersistentData().getInt(NBT_CHARGE);
        player.getPersistentData().remove(NBT_CHARGE);
        Hand hand = handWithPlainBall(player);
        if (hand == null || !player.isAlive() || player.getCooldowns().isOnCooldown(InitItems.STEEL_BALL.get())) {
            return;
        }
        ItemStack stack = player.getItemInHand(hand);
        double fraction = SpinCharge.cap(SpinCharge.fraction(charge, maxTicks()), SteelBallItem.isChipped(stack), chippedMax());
        ItemStack thrown = stack.copy();
        thrown.setCount(1);
        if (!player.abilities.instabuild) {
            stack.shrink(1);
        }
        SteelBallItem.throwBall(world, player, thrown, false, fraction);
        player.swing(hand, true);
        if (fraction > 0) {
            ((ServerWorld) world).sendParticles(ParticleTypes.CRIT, player.getX(), player.getEyeY() - 0.2, player.getZ(),
                    (int) (6 + 14 * fraction), 0.2, 0.2, 0.2, 0.3);
        }
    }

    // Client visuals. RotP calls this on the client for the user and for players tracking him.

    /**
     * Sparks circle the hand with the ball; the circle tightens and spins faster as the charge grows,
     * and turns golden at full charge. The charge settings come from the server on login
     * ({@code SpinChargeConfigPacket}); the local config is only a fallback until then.
     */
    @Override
    public void onHoldTickClientEffect(LivingEntity user, INonStandPower power, int ticksHeld,
            boolean requirementsFulfilled, boolean stateRefreshed) {
        if (!requirementsFulfilled || !user.level.isClientSide() || !(user instanceof PlayerEntity)) {
            return;
        }
        Hand hand = handWithPlainBall((PlayerEntity) user);
        if (hand == null) {
            return;
        }
        boolean chipped = SteelBallItem.isChipped(user.getItemInHand(hand));
        int maxTicks = ClientSpinState.chargeMaxTicks(maxTicks());
        double chippedMax = ClientSpinState.chargeChippedMax(chippedMax());
        double fraction = SpinCharge.cap(SpinCharge.fraction(ticksHeld, maxTicks), chipped, chippedMax);
        boolean full = SpinCharge.isFull(ticksHeld, maxTicks, chipped, chippedMax);

        HandSide side = hand == Hand.MAIN_HAND ? user.getMainArm() : user.getMainArm().getOpposite();
        double sign = side == HandSide.RIGHT ? 1 : -1;
        float yaw = user.yBodyRot * ((float) Math.PI / 180F);
        double fwdX = -MathHelper.sin(yaw);
        double fwdZ = MathHelper.cos(yaw);
        double rightX = -MathHelper.cos(yaw) * sign;
        double rightZ = -MathHelper.sin(yaw) * sign;
        double cx = user.getX() + rightX * 0.4 + fwdX * 0.35;
        double cy = user.getY() + user.getBbHeight() * 0.5;
        double cz = user.getZ() + rightZ * 0.4 + fwdZ * 0.35;

        double radius = 0.6 - 0.45 * fraction;
        float angle = ticksHeld * (0.5F + 0.8F * (float) fraction);
        int arms = full ? 3 : 2;
        for (int i = 0; i < arms; i++) {
            double a = angle + i * 2 * Math.PI / arms;
            user.level.addParticle(full ? ParticleTypes.ENCHANTED_HIT : ParticleTypes.CRIT,
                    cx + Math.cos(a) * radius, cy + Math.sin(a * 2) * 0.05, cz + Math.sin(a) * radius, 0, 0, 0);
        }
        if (full) {
            user.level.addParticle(GOLD_DUST, cx, cy, cz, 0, 0, 0);
        }
        else if (ticksHeld % 3 == 0) {
            user.level.addParticle(GOLD_DUST_SMALL, cx, cy, cz, 0, 0, 0);
        }
    }

    /** Wind-up pose: the ball arm drawn back, the torso turned (playerAnimator, optional). */
    @Override
    public boolean clHeldStartAnim(PlayerEntity user) {
        return SpinPlayerAnimations.setBallCharge(user, true);
    }

    @Override
    public void clHeldStopAnim(PlayerEntity user) {
        SpinPlayerAnimations.setBallCharge(user, false);
    }

    /** Only the ordinary steel ball: the Wrecking Ball (a subclass) is excluded. */
    @Nullable
    public static Hand handWithPlainBall(PlayerEntity player) {
        if (player.getMainHandItem().getItem() == InitItems.STEEL_BALL.get()) {
            return Hand.MAIN_HAND;
        }
        if (player.getOffhandItem().getItem() == InitItems.STEEL_BALL.get()) {
            return Hand.OFF_HAND;
        }
        return null;
    }
}
