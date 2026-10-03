package com.loischsiy.rotpspin.compat.d4c;

import java.util.function.BooleanSupplier;

import com.loischsiy.rotpspin.config.SpinConfig;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Optional D4C addon (rotp_d4c, docs/integrations.md): Ball Breaker pierces Love Train
 * (SBR ch. 83-84). Love Train cancels every attack and hurt event on its holder and denies
 * harmful effects at HIGHEST priority; while a Ball Breaker hit is in progress our LOWEST
 * listeners lift exactly those vetoes for the Love Train holder.
 *
 * No compile-time dependency on D4C: the effect is looked up by registry name, so this class
 * is safe to call without the mod (every call then falls through to vanilla behaviour).
 */
public final class LoveTrainBypass {
    public static final ResourceLocation LOVE_TRAIN_ID = new ResourceLocation("rotp_d4c", "love_train");

    private static boolean loaded;
    private static Effect loveTrain;
    private static final ThreadLocal<int[]> DEPTH = ThreadLocal.withInitial(() -> new int[1]);

    private LoveTrainBypass() {}

    /** Called once from AddonMain when rotp_d4c is present; the listeners go on the Forge bus. */
    public static void markLoaded() {
        loaded = true;
    }

    /** Pure rule: lift D4C's veto only inside a Ball Breaker hit on a Love Train holder. */
    public static boolean shouldLift(boolean enabled, boolean piercing, boolean hasLoveTrain) {
        return enabled && piercing && hasLoveTrain;
    }

    /** Runs a Ball Breaker damage call; Love Train does not stop it. */
    public static boolean pierce(BooleanSupplier hit) {
        if (!loaded) {
            return hit.getAsBoolean();
        }
        int[] depth = DEPTH.get();
        depth[0]++;
        try {
            return hit.getAsBoolean();
        } finally {
            depth[0]--;
        }
    }

    /**
     * Ball Breaker's effects reach a Love Train holder. forceAddEffect skips PotionAddedEvent,
     * which D4C tries to cancel (non-cancelable in Forge 36: it would throw). Our callers compute
     * the final amplifier/duration themselves, so the missing merge is harmless.
     */
    public static void addEffect(LivingEntity target, EffectInstance effect) {
        if (!active() || !hasLoveTrain(target)) {
            target.addEffect(effect);
            return;
        }
        pierce(() -> {
            target.forceAddEffect(effect);
            return true;
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onAttack(LivingAttackEvent event) {
        if (event.isCanceled() && lift(event.getEntityLiving())) {
            event.setCanceled(false);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onHurt(LivingHurtEvent event) {
        if (event.isCanceled() && lift(event.getEntityLiving())) {
            event.setCanceled(false);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPotionApplicable(PotionEvent.PotionApplicableEvent event) {
        if (event.getResult() == Event.Result.DENY && lift(event.getEntityLiving())) {
            // DEFAULT, not ALLOW: vanilla immunities (e.g. undead vs. poison) still apply.
            event.setResult(Event.Result.DEFAULT);
        }
    }

    private static boolean active() {
        return loaded && SpinConfig.COMPAT_D4C_ENABLED.get();
    }

    private static boolean lift(LivingEntity entity) {
        return loaded && shouldLift(SpinConfig.COMPAT_D4C_ENABLED.get(), DEPTH.get()[0] > 0, hasLoveTrain(entity));
    }

    private static boolean hasLoveTrain(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        if (loveTrain == null) {
            loveTrain = ForgeRegistries.POTIONS.getValue(LOVE_TRAIN_ID);
            if (loveTrain == null) {
                return false;
            }
        }
        return entity.hasEffect(loveTrain);
    }
}
