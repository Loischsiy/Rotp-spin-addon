package com.loischsiy.rotpspin.power;

import java.util.Locale;

import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.init.InitPowers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * Spin on one's own body ({@link com.loischsiy.rotpspin.action.SpinBodyBrace}): while the body is
 * rigid, part of a kinetic blow is absorbed for Spin energy, the body is not knocked back, and a melee
 * attacker gets the energy passed back as knockback. Every absorbed blow is heard and seen (a clang,
 * sparks, the absorbed amount on the action bar), otherwise a reduced mob hit looks like a normal one.
 * Math in {@link SpinBrace}.
 */
@EventBusSubscriber(modid = AddonMain.MOD_ID)
public class SpinBraceHandler {

    private static boolean isBracedNow(INonStandPower power) {
        return power.getHeldAction() == InitPowers.SPIN_BODY_BRACE.get()
                && SpinBrace.isBraced(power.getHeldActionTicks(), SpinConfig.BRACE_WINDUP_TICKS.get());
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity.level.isClientSide()) {
            return;
        }
        DamageSource source = event.getSource();
        Entity direct = source.getDirectEntity();
        boolean melee = direct instanceof LivingEntity && direct == source.getEntity();
        if (!SpinBrace.isBraceable(source.isProjectile(), source.isExplosion(), melee,
                source.isBypassArmor(), source.isFire(), source.isMagic())) {
            return;
        }
        INonStandPower.getNonStandPowerOptional(entity).ifPresent(power -> {
            if (!isBracedNow(power)) {
                return;
            }
            boolean creative = power.isUserCreative();
            float perDamage = creative ? 0 : SpinConfig.BRACE_ENERGY_PER_DAMAGE.get().floatValue();
            float energy = power.getEnergy();
            float absorbed = SpinBrace.absorbed(event.getAmount(),
                    SpinConfig.BRACE_DAMAGE_REDUCTION.get().floatValue(), energy, perDamage);
            if (absorbed <= 0) {
                return;
            }
            float cost = SpinBrace.energyCost(absorbed, perDamage, energy);
            if (cost > 0 && !power.consumeEnergy(cost)) {
                return;
            }
            event.setAmount(event.getAmount() - absorbed);

            double knockback = SpinConfig.BRACE_ATTACKER_KNOCKBACK.get();
            if (melee && knockback > 0) {
                LivingEntity attacker = (LivingEntity) direct;
                attacker.knockback((float) knockback,
                        entity.getX() - attacker.getX(), entity.getZ() - attacker.getZ());
                attacker.hurtMarked = true;
            }
            feedback(entity, absorbed, event.getAmount());
        });
    }

    /** The rigid body stands its ground: no knockback while braced. */
    @SubscribeEvent
    public static void onKnockBack(LivingKnockBackEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity.level.isClientSide() || !SpinConfig.BRACE_NO_KNOCKBACK.get()) {
            return;
        }
        INonStandPower.getNonStandPowerOptional(entity).ifPresent(power -> {
            if (isBracedNow(power)) {
                event.setCanceled(true);
            }
        });
    }

    private static void feedback(LivingEntity entity, float absorbed, float taken) {
        ServerWorld world = (ServerWorld) entity.level;
        world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.SHIELD_BLOCK,
                entity.getSoundSource(), 1.0F, 0.8F + world.random.nextFloat() * 0.3F);
        world.sendParticles(ParticleTypes.CRIT,
                entity.getX(), entity.getY(0.5), entity.getZ(), 14, 0.35, 0.5, 0.35, 0.25);
        world.sendParticles(ParticleTypes.ENCHANTED_HIT,
                entity.getX(), entity.getY(0.6), entity.getZ(), 8, 0.3, 0.4, 0.3, 0.1);
        if (entity instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity) entity).displayClientMessage(new TranslationTextComponent(
                    "rotp_spin.message.brace_absorbed", format(absorbed), format(taken))
                    .withStyle(TextFormatting.AQUA), true);
        }
    }

    private static String format(float value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
