package com.loischsiy.rotpspin.power;

import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.init.InitPowers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * Spin on one's own body ({@link com.loischsiy.rotpspin.action.SpinBodyBrace}): while the body is
 * rigid, part of a kinetic blow is absorbed for Spin energy and a melee attacker gets the energy
 * passed back as knockback. Math in {@link SpinBrace}.
 */
@EventBusSubscriber(modid = AddonMain.MOD_ID)
public class SpinBraceHandler {

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
            if (power.getHeldAction() != InitPowers.SPIN_BODY_BRACE.get()
                    || !SpinBrace.isBraced(power.getHeldActionTicks(), SpinConfig.BRACE_WINDUP_TICKS.get())) {
                return;
            }
            boolean creative = power.isUserCreative();
            float perDamage = creative ? 0 : SpinConfig.BRACE_ENERGY_PER_DAMAGE.get().floatValue();
            float absorbed = SpinBrace.absorbed(event.getAmount(),
                    SpinConfig.BRACE_DAMAGE_REDUCTION.get().floatValue(), power.getEnergy(), perDamage);
            if (absorbed <= 0 || (perDamage > 0 && !power.consumeEnergy(absorbed * perDamage))) {
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
            ((ServerWorld) entity.level).sendParticles(ParticleTypes.CRIT,
                    entity.getX(), entity.getY(0.5), entity.getZ(), 6, 0.3, 0.4, 0.3, 0.1);
        });
    }
}
