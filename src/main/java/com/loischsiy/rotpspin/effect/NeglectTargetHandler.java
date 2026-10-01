package com.loischsiy.rotpspin.effect;

import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.entity.WreckingBall;
import com.loischsiy.rotpspin.init.InitEffects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * Hemispatial neglect, targeting half: a neglected mob cannot even pick a new target on its left
 * (the effect tick only drops a target it already had, the target goals would re-pick it in
 * between). The event is not cancelable in Forge 36: {@code MobEntity#setTarget} assigns the
 * target first and then fires it, so clearing it here is the usual pattern.
 */
@EventBusSubscriber(modid = AddonMain.MOD_ID)
public class NeglectTargetHandler {

    @SubscribeEvent
    public static void onSetAttackTarget(LivingSetAttackTargetEvent event) {
        LivingEntity target = event.getTarget();
        if (target == null || !(event.getEntityLiving() instanceof MobEntity)) {
            return;
        }
        MobEntity mob = (MobEntity) event.getEntityLiving();
        if (mob.level.isClientSide() || !SpinConfig.WRECKING_NEGLECT_BLOCKS_RETARGET.get()
                || !mob.hasEffect(InitEffects.NEGLECT.get())) {
            return;
        }
        if (isOnLeft(mob, target) && mob.getTarget() == target) {
            mob.setTarget(null);
        }
    }

    /** The target stands in the neglected (left) half of the mob's view. */
    static boolean isOnLeft(LivingEntity viewer, LivingEntity target) {
        Vector3d toTarget = new Vector3d(target.getX() - viewer.getX(), 0, target.getZ() - viewer.getZ());
        return toTarget.lengthSqr() > 1e-6 && WreckingBall.isOnLeftSide(viewer.getLookAngle(), toTarget);
    }
}
