package com.loischsiy.rotpspin.effect;

import com.loischsiy.rotpspin.config.SpinConfig;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

/**
 * Desiccation from a spinning ball hit (docs/spin-lore.md: the ball "wrings water out of the body").
 * The victim dries out: a player burns food faster, and a burning target is put out by the
 * water pressed out of it. Bonus damage to water mobs is dealt once on the hit (SteelBallEntity).
 */
public class DesiccationEffect extends Effect {

    public DesiccationEffect() {
        super(EffectType.HARMFUL, 0xc2a36b);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level.isClientSide()) {
            return;
        }
        if (entity instanceof PlayerEntity) {
            ((PlayerEntity) entity).causeFoodExhaustion(
                    SpinConfig.SQUEEZE_DRY_EXHAUSTION_PER_TICK.get().floatValue() * (amplifier + 1));
        }
        if (entity.isOnFire()) {
            entity.clearFire();
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
