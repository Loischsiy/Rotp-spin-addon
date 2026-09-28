package com.loischsiy.rotpspin.effect;

import com.loischsiy.rotpspin.entity.WreckingBall;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.math.vector.Vector3d;

/**
 * Hemispatial neglect from a Wrecking Ball shockwave: the victim stops noticing
 * whatever is on its left side and drops such targets (docs/spin-lore.md).
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
            if (target != null && target.isAlive()) {
                Vector3d toTarget = new Vector3d(target.getX() - mob.getX(), 0, target.getZ() - mob.getZ());
                if (toTarget.lengthSqr() > 1e-6 && WreckingBall.isOnLeftSide(mob.getLookAngle(), toTarget)) {
                    mob.setTarget(null);
                }
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }
}
