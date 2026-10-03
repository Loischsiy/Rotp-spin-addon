package com.loischsiy.rotpspin.action;

import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.init.InitEffects;
import com.loischsiy.rotpspin.power.BallBreakerManifestation;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.world.World;

/**
 * Ball Breaker's touch of senescence (docs/spin-lore.md): everything in the touched zone —
 * living and Stands alike — rapidly ages. Armor-piercing, like the canon Love Train bypass.
 * A chipped ball in the master's hand weakens it (ch. 84: Valentine survived).
 */
public class BallBreakerSenescence extends StandEntityAction {
    public static final StandPose SENESCENCE_POSE = new StandPose("senescence_touch");

    public BallBreakerSenescence(StandEntityAction.Builder builder) {
        super(builder);
    }

    @Override
    public float getStaminaCost(IStandPower stand) {
        return SpinConfig.BALL_BREAKER_TOUCH_STAMINA.get().floatValue();
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (world.isClientSide()) {
            return;
        }
        LivingEntity user = userPower.getUser();
        double range = SpinConfig.BALL_BREAKER_TOUCH_RANGE.get();
        double scale = BallBreakerManifestation.senescenceScale(BallBreakerManifestation.holdsChippedBall(user),
                SpinConfig.BALL_BREAKER_CHIPPED_RETENTION.get());
        float damage = (float) (SpinConfig.BALL_BREAKER_TOUCH_DAMAGE.get() * scale);
        int duration = Math.max(1, (int) Math.round(SpinConfig.BALL_BREAKER_SENESCENCE_DURATION.get() * scale));
        for (LivingEntity victim : world.getEntitiesOfClass(LivingEntity.class,
                standEntity.getBoundingBox().inflate(range),
                e -> e.isAlive() && e != user && e != standEntity)) {
            victim.hurt(new EntityDamageSource("ballBreaker", standEntity).bypassArmor(), damage);
            victim.addEffect(new EffectInstance(InitEffects.SENESCENCE.get(), duration));
            victim.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, duration, 1));
        }
    }
}
