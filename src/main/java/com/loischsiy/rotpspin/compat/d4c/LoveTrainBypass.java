package com.loischsiy.rotpspin.compat.d4c;

import java.util.function.BooleanSupplier;

import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.init.InitEffects;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
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
 * listeners lift exactly those vetoes for the Love Train holder. Love Train also strips every
 * harmful effect on each tick of its own effect; senescence and the effects Ball Breaker itself
 * forced onto the holder are exempt from that cleanse only (milk and other removals behave as usual).
 *
 * No compile-time dependency on D4C: the effect is looked up by registry name, so this class
 * is safe to call without the mod (every call then falls through to vanilla behaviour).
 */
public final class LoveTrainBypass {
    public static final ResourceLocation LOVE_TRAIN_ID = new ResourceLocation("rotp_d4c", "love_train");

    private static boolean loaded;
    private static Effect loveTrain;
    private static final ThreadLocal<int[]> DEPTH = ThreadLocal.withInitial(() -> new int[1]);
    /** persistentData: effect id -> game time until which Love Train may not wash it away. */
    private static final String KEPT_KEY = "rotp_spin_lt_kept";

    private LoveTrainBypass() {}

    /** Called once from AddonMain when rotp_d4c is present; the listeners go on the Forge bus. */
    public static void markLoaded() {
        loaded = true;
    }

    /** Pure rule: lift D4C's veto only inside a Ball Breaker hit on a Love Train holder. */
    public static boolean shouldLift(boolean enabled, boolean piercing, boolean hasLoveTrain) {
        return enabled && piercing && hasLoveTrain;
    }

    /** Pure rule: keep a Ball Breaker effect when Love Train's own tick tries to wash it away. */
    public static boolean shouldKeep(boolean enabled, boolean fromBallBreaker, boolean hasLoveTrain,
            boolean fromLoveTrainTick) {
        return enabled && fromBallBreaker && hasLoveTrain && fromLoveTrainTick;
    }

    /** Pure rule: a forced effect stays exempt while its Ball Breaker duration lasts. */
    public static boolean keptUntil(long expiresAt, long gameTime) {
        return gameTime <= expiresAt;
    }

    /**
     * Damage of Ball Breaker's touch. D4C shunts a Love Train holder's misfortune onto bystanders
     * unless the damage is a projectile, but Ball Breaker reaches the holder itself (SBR ch. 83-84).
     * So with D4C the source reports "projectile" only until the hurt event settles (our LOWEST
     * listener); armor enchantments read it later, so Projectile Protection does not apply.
     */
    public static DamageSource touchSource(Entity stand) {
        return loaded ? new TouchSource(stand) : new EntityDamageSource("ballBreaker", stand).bypassArmor();
    }

    private static final class TouchSource extends EntityDamageSource {
        private boolean settled;

        TouchSource(Entity stand) {
            super("ballBreaker", stand);
            bypassArmor();
        }

        @Override
        public boolean isProjectile() {
            return !settled;
        }
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
        if (effect.getEffect() != InitEffects.SENESCENCE.get() && effect.getEffect().getRegistryName() != null) {
            CompoundNBT data = target.getPersistentData();
            CompoundNBT kept = data.getCompound(KEPT_KEY);
            kept.putLong(effect.getEffect().getRegistryName().toString(),
                    target.level.getGameTime() + effect.getDuration());
            data.put(KEPT_KEY, kept);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onAttack(LivingAttackEvent event) {
        if (event.isCanceled() && lift(event.getEntityLiving())) {
            event.setCanceled(false);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onHurt(LivingHurtEvent event) {
        if (event.getSource() instanceof TouchSource) {
            ((TouchSource) event.getSource()).settled = true;
        }
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPotionRemove(PotionEvent.PotionRemoveEvent event) {
        LivingEntity entity = event.getEntityLiving();
        Effect effect = event.getPotion();
        if (!active() || effect == null || !hasLoveTrain(entity) || !fromBallBreaker(entity, effect)) {
            return;
        }
        if (shouldKeep(true, true, true, calledFromLoveTrain())) {
            event.setCanceled(true);
        }
    }

    private static boolean fromBallBreaker(LivingEntity entity, Effect effect) {
        if (effect == InitEffects.SENESCENCE.get()) {
            return true;
        }
        CompoundNBT kept = entity.getPersistentData().getCompound(KEPT_KEY);
        String id = effect.getRegistryName() == null ? null : effect.getRegistryName().toString();
        if (id == null || !kept.contains(id)) {
            return false;
        }
        if (keptUntil(kept.getLong(id), entity.level.getGameTime())) {
            return true;
        }
        kept.remove(id);
        return false;
    }

    /** True if Love Train's Effect class is on the stack (its per-tick harmful-effect cleanse). */
    private static boolean calledFromLoveTrain() {
        // Java 8 target: no StackWalker. Runs only when senescence leaves a Love Train holder.
        String loveTrainClass = loveTrain.getClass().getName();
        StackTraceElement[] stack = new Throwable().getStackTrace();
        for (int i = 0; i < stack.length && i < 32; i++) {
            if (loveTrainClass.equals(stack[i].getClassName())) {
                return true;
            }
        }
        return false;
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
