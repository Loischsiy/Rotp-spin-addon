package com.loischsiy.rotpspin.effect;

import com.loischsiy.rotpspin.config.SpinConfig;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;

/**
 * Senescence from Ball Breaker's touch (docs/spin-lore.md): the victim rapidly ages
 * in the touched zone. Wither-like decay that ignores armor, as the canon bypass.
 */
public class SenescenceEffect extends Effect {

    public SenescenceEffect() {
        super(EffectType.HARMFUL, 0x7a7a8a);
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
