package com.loischsiy.rotpspin.effect;

import java.util.UUID;

import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.power.BallBreakerAging;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;

/**
 * Senescence from Ball Breaker's touch (docs/spin-lore.md): the victim rapidly ages
 * in the touched zone (SBR ch. 83-84). Wither-like decay that ignores armor, as the canon bypass.
 * Each level is one more touch: the aging body loses max health, speed and strength
 * (game form of aging, numbers in the ball_breaker config).
 */
public class SenescenceEffect extends Effect {
    private static final String HEALTH_ID = "5f1e3a0c-8d7b-4b1e-9b52-6a0c2f3e7d01";
    private static final String SPEED_ID = "5f1e3a0c-8d7b-4b1e-9b52-6a0c2f3e7d02";
    private static final String ATTACK_ID = "5f1e3a0c-8d7b-4b1e-9b52-6a0c2f3e7d03";
    private static final UUID HEALTH_UUID = UUID.fromString(HEALTH_ID);
    private static final UUID SPEED_UUID = UUID.fromString(SPEED_ID);

    public SenescenceEffect() {
        super(EffectType.HARMFUL, 0x7a7a8a);
        // Amounts are placeholders: the real per-level values come from the config at apply time.
        addAttributeModifier(Attributes.MAX_HEALTH, HEALTH_ID, -1, AttributeModifier.Operation.ADDITION);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, SPEED_ID, -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, ATTACK_ID, -1, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public double getAttributeModifierValue(int amplifier, AttributeModifier modifier) {
        double perLevel;
        if (HEALTH_UUID.equals(modifier.getId())) {
            perLevel = SpinConfig.BALL_BREAKER_HEALTH_PER_STACK.get();
        } else if (SPEED_UUID.equals(modifier.getId())) {
            perLevel = SpinConfig.BALL_BREAKER_SPEED_PER_STACK.get();
        } else {
            perLevel = SpinConfig.BALL_BREAKER_ATTACK_PER_STACK.get();
        }
        return BallBreakerAging.attributePenalty(perLevel, amplifier + 1);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeModifierManager attributes, int amplifier) {
        super.addAttributeModifiers(entity, attributes, amplifier);
        // An aged body cannot hold more health than its shrunken maximum.
        if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level.isClientSide()) {
            entity.hurt(DamageSource.WITHER,
                    SpinConfig.BALL_BREAKER_SENESCENCE_DAMAGE.get().floatValue() * (amplifier + 1));
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % SpinConfig.BALL_BREAKER_SENESCENCE_INTERVAL.get() == 0;
    }
}
