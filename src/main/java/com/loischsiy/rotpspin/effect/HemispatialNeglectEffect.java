package com.loischsiy.rotpspin.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

/**
 * Hemispatial neglect from a Wrecking Ball shockwave: the victim stops noticing
 * whatever is on its left side and drops such targets (docs/spin-lore.md). New targets on the
 * left are refused by {@link NeglectTargetHandler}; the player's veil and the horse drifting right
 * live in {@code client.NeglectClient}.
 */
public class HemispatialNeglectEffect extends Effect {

    public HemispatialNeglectEffect() {
        super(EffectType.HARMFUL, 0x8a7bd8);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level.isClientSide() && entity instanceof MobEntity) {
            MobEntity mob = (MobEntity) entity;
            LivingEntity target = mob.getTarget();
            if (target != null && target.isAlive() && NeglectTargetHandler.isOnLeft(mob, target)) {
                mob.setTarget(null);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }
}
