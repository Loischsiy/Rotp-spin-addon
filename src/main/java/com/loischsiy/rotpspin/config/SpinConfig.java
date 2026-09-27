package com.loischsiy.rotpspin.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class SpinConfig {
    public static final ForgeConfigSpec SPEC;

    // Spin energy
    public static final ForgeConfigSpec.DoubleValue ENERGY_MAX;
    public static final ForgeConfigSpec.DoubleValue ENERGY_REGEN_PER_TICK;

    // Lesson 1: Spin leap
    public static final ForgeConfigSpec.DoubleValue LEAP_STRENGTH;
    public static final ForgeConfigSpec.IntValue LEAP_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.DoubleValue LEAP_ENERGY_COST;
    public static final ForgeConfigSpec.BooleanValue LEAP_IGNORE_SLOWNESS;
    public static final ForgeConfigSpec.DoubleValue LEAP_FALL_REDUCTION;

    // Spin actions (RotP hotbars)
    public static final ForgeConfigSpec.DoubleValue STEER_ENERGY_PER_TICK;
    public static final ForgeConfigSpec.DoubleValue STEER_TURN_RATE;
    public static final ForgeConfigSpec.DoubleValue STEER_MIN_SPEED;
    public static final ForgeConfigSpec.DoubleValue STEER_AIM_DISTANCE;
    public static final ForgeConfigSpec.DoubleValue STEER_SEARCH_RANGE;
    public static final ForgeConfigSpec.DoubleValue HIJACK_ENERGY_COST;
    public static final ForgeConfigSpec.DoubleValue HIJACK_REACH;
    public static final ForgeConfigSpec.IntValue HIJACK_STUN_TICKS;
    public static final ForgeConfigSpec.IntValue HIJACK_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.BooleanValue HIJACK_FORCED_ACTION;
    public static final ForgeConfigSpec.DoubleValue HIJACK_FORCED_RANGE;
    public static final ForgeConfigSpec.BooleanValue HIJACK_DISARM_PLAYERS;
    public static final ForgeConfigSpec.DoubleValue ITEM_SPIN_ENERGY_COST;
    public static final ForgeConfigSpec.DoubleValue ITEM_SPIN_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue ITEM_SPIN_VELOCITY;
    public static final ForgeConfigSpec.IntValue ITEM_SPIN_COOLDOWN_TICKS;

    // Lesson progression
    public static final ForgeConfigSpec.BooleanValue LESSONS_ENABLED;
    public static final ForgeConfigSpec.IntValue LESSON2_BALL_HITS;
    public static final ForgeConfigSpec.IntValue LESSON3_HIJACKS;

    public static final ForgeConfigSpec.DoubleValue HEALING_ENERGY_PER_TICK;
    public static final ForgeConfigSpec.IntValue HEALING_INTERVAL_TICKS;
    public static final ForgeConfigSpec.DoubleValue HEALING_AMOUNT;
    public static final ForgeConfigSpec.DoubleValue HEALING_TARGET_REACH;
    public static final ForgeConfigSpec.BooleanValue HEALING_CURES_HARMFUL;

    // Steel ball
    public static final ForgeConfigSpec.DoubleValue BALL_SPIN_COST;
    public static final ForgeConfigSpec.DoubleValue BALL_SPIN_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue BALL_PLAIN_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue BALL_SPIN_VELOCITY;
    public static final ForgeConfigSpec.DoubleValue BALL_PLAIN_VELOCITY;
    public static final ForgeConfigSpec.DoubleValue BALL_INACCURACY;
    public static final ForgeConfigSpec.IntValue BALL_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.IntValue BALL_RETURN_AFTER_TICKS;
    public static final ForgeConfigSpec.IntValue BALL_MAX_RETURN_TICKS;
    public static final ForgeConfigSpec.DoubleValue BALL_RETURN_ACCELERATION;
    public static final ForgeConfigSpec.DoubleValue BALL_DAMAGED_CHANCE_ON_BLOCK_HIT;
    public static final ForgeConfigSpec.DoubleValue BALL_DAMAGED_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue BALL_RICOCHET_MAX_BOUNCES;
    public static final ForgeConfigSpec.DoubleValue BALL_RICOCHET_SPEED_RETENTION;
    public static final ForgeConfigSpec.DoubleValue BALL_RICOCHET_MIN_SPEED;
    public static final ForgeConfigSpec.ConfigValue<String> BALL_REPAIR_MATERIAL;
    public static final ForgeConfigSpec.IntValue BALL_REPAIR_LEVEL_COST;

    // Gyro's holster
    public static final ForgeConfigSpec.IntValue HOLSTER_CAPACITY;

    // Optional integrations
    public static final ForgeConfigSpec.BooleanValue COMPAT_CURIOS_ENABLED;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("spin_energy");
        ENERGY_MAX = b.comment("Maximum Spin energy a player can hold.")
                .defineInRange("max", 100.0, 1.0, 10000.0);
        ENERGY_REGEN_PER_TICK = b.comment("Spin energy restored per tick (20 ticks = 1 second).")
                .defineInRange("regenPerTick", 0.25, 0.0, 1000.0);
        b.pop();

        b.push("spin_leap");
        LEAP_STRENGTH = b.comment("Lesson 1 \"If there is a will, do it\": strength of the Spin leap (Shift + Jump on the ground). RotP's Hamon leap is 1.4.")
                .defineInRange("strength", 1.3, 0.0, 10.0);
        LEAP_COOLDOWN_TICKS = b.comment("Cooldown between Spin leaps, in ticks.")
                .defineInRange("cooldownTicks", 30, 0, 1200);
        LEAP_ENERGY_COST = b.comment("Spin energy consumed by a leap.")
                .defineInRange("energyCost", 25.0, 0.0, 10000.0);
        LEAP_IGNORE_SLOWNESS = b.comment("The leap is driven by will, not by the legs: slowness does not weaken it (speed boosts still help).")
                .define("ignoreSlowness", true);
        LEAP_FALL_REDUCTION = b.comment("Fall distance (blocks) forgiven to a Spin user. Replaces RotP's generic leap bonus (~19 blocks): Spin does not make the body superhuman.")
                .defineInRange("fallDistanceReduction", 0.0, 0.0, 256.0);
        b.pop();

        b.push("ball_steer");
        STEER_ENERGY_PER_TICK = b.comment("Ability \"Steel Ball Control\" (hold): Spin energy consumed per tick while steering a ball in flight.")
                .defineInRange("energyPerTick", 0.5, 0.0, 1000.0);
        STEER_TURN_RATE = b.comment("How sharply the ball turns towards the aim point each tick (0 = no turn, 1 = instant).")
                .defineInRange("turnRate", 0.25, 0.0, 1.0);
        STEER_MIN_SPEED = b.comment("A steered ball keeps at least this speed (blocks per tick), so its rotation does not fade while it is held.")
                .defineInRange("minSpeed", 1.2, 0.0, 10.0);
        STEER_AIM_DISTANCE = b.comment("The ball flies towards the point this many blocks ahead along the user's look.")
                .defineInRange("aimDistance", 32.0, 1.0, 256.0);
        STEER_SEARCH_RANGE = b.comment("Only the user's own spinning ball within this distance can be steered.")
                .defineInRange("searchRange", 48.0, 1.0, 256.0);
        b.pop();

        b.push("muscle_hijack");
        HIJACK_ENERGY_COST = b.comment("Lesson 2 \"Use your muscles\": Spin energy consumed by sending rotation into a touched body.")
                .defineInRange("energyCost", 30.0, 0.0, 10000.0);
        HIJACK_REACH = b.comment("Maximum distance to the target (blocks): the technique needs a touch.")
                .defineInRange("reach", 3.0, 0.5, 16.0);
        HIJACK_STUN_TICKS = b.comment("How long the target's muscles stay seized (RotP stun), in ticks.")
                .defineInRange("stunTicks", 60, 1, 1200);
        HIJACK_COOLDOWN_TICKS = b.comment("Cooldown of the technique, in ticks.")
                .defineInRange("cooldownTicks", 100, 0, 6000);
        HIJACK_FORCED_ACTION = b.comment("Before the muscles seize, the hijacked mob makes one forced move: a ranged mob shoots, a melee mob strikes the nearest other creature (never the Spin user).")
                .define("forcedAction", true);
        HIJACK_FORCED_RANGE = b.comment("How far (blocks) from the hijacked mob a creature can be to become the target of its forced move.")
                .defineInRange("forcedActionRange", 6.0, 1.0, 32.0);
        HIJACK_DISARM_PLAYERS = b.comment("A hijacked player's hand opens: the main hand item is dropped.")
                .define("disarmPlayers", true);
        b.pop();

        b.push("item_spin");
        ITEM_SPIN_ENERGY_COST = b.comment("Lesson 3 \"Believe in the rotation\": Spin energy consumed by throwing a held item (not a steel ball) with rotation.")
                .defineInRange("energyCost", 15.0, 0.0, 10000.0);
        ITEM_SPIN_DAMAGE = b.comment("Base damage of a spinning item (multiplied by its velocity, like an arrow).")
                .defineInRange("damage", 3.0, 0.0, 1000.0);
        ITEM_SPIN_VELOCITY = b.comment("Launch velocity of a spinning item (blocks per tick).")
                .defineInRange("velocity", 1.4, 0.1, 10.0);
        ITEM_SPIN_COOLDOWN_TICKS = b.comment("Cooldown of the technique, in ticks.")
                .defineInRange("cooldownTicks", 20, 0, 1200);
        b.pop();

        b.push("lessons");
        LESSONS_ENABLED = b.comment("Gyro's lessons unlock the techniques step by step. false = everything is available at once.")
                .define("enabled", true);
        LESSON2_BALL_HITS = b.comment("Lesson 2 \"Use your muscles\" is learned after this many hits on creatures with a spinning steel ball.")
                .defineInRange("lesson2BallHits", 10, 0, 10000);
        LESSON3_HIJACKS = b.comment("Lesson 3 \"Believe in the rotation\" is learned after this many successful Muscle Hijacks.")
                .defineInRange("lesson3Hijacks", 5, 0, 10000);
        b.pop();

        b.push("zeppeli_healing");
        HEALING_ENERGY_PER_TICK = b.comment("Ability \"Zeppeli Medicine\" (hold): Spin energy consumed per tick of treatment.")
                .defineInRange("energyPerTick", 0.4, 0.0, 1000.0);
        HEALING_INTERVAL_TICKS = b.comment("Health is restored once per this many ticks of holding.")
                .defineInRange("intervalTicks", 20, 1, 1200);
        HEALING_AMOUNT = b.comment("Health restored per interval (2 = one heart).")
                .defineInRange("amount", 1.0, 0.0, 100.0);
        HEALING_TARGET_REACH = b.comment("With Sneak held, the entity under the crosshair within this distance is treated instead of the user.")
                .defineInRange("targetReach", 3.0, 0.5, 16.0);
        HEALING_CURES_HARMFUL = b.comment("Each interval also removes poison, wither and RotP bleeding.")
                .define("curesHarmfulEffects", true);
        b.pop();

        b.push("steel_ball");
        BALL_SPIN_COST = b.comment("Spin energy consumed by a throw with Spin. Without enough energy the ball is thrown plainly: no return, lower damage.")
                .defineInRange("spinCost", 20.0, 0.0, 10000.0);
        BALL_SPIN_DAMAGE = b.comment("Base damage of a spinning ball (multiplied by its velocity, like an arrow).")
                .defineInRange("spinDamage", 4.0, 0.0, 1000.0);
        BALL_PLAIN_DAMAGE = b.comment("Base damage of a ball thrown without Spin.")
                .defineInRange("plainDamage", 2.0, 0.0, 1000.0);
        BALL_SPIN_VELOCITY = b.comment("Launch velocity of a spinning ball (blocks per tick).")
                .defineInRange("spinVelocity", 1.6, 0.1, 10.0);
        BALL_PLAIN_VELOCITY = b.comment("Launch velocity of a ball thrown without Spin.")
                .defineInRange("plainVelocity", 1.0, 0.1, 10.0);
        BALL_INACCURACY = b.comment("Throw inaccuracy (vanilla arrow semantics).")
                .defineInRange("inaccuracy", 0.5, 0.0, 10.0);
        BALL_COOLDOWN_TICKS = b.comment("Item cooldown after a throw, in ticks.")
                .defineInRange("cooldownTicks", 10, 0, 1200);
        BALL_RETURN_AFTER_TICKS = b.comment("A spinning ball starts returning to the thrower after this many ticks of flight (or earlier, on hit).")
                .defineInRange("returnAfterTicks", 20, 1, 1200);
        BALL_MAX_RETURN_TICKS = b.comment("If the ball has not reached the thrower within this many ticks of returning, it loses its rotation and drops.")
                .defineInRange("maxReturnTicks", 200, 1, 6000);
        BALL_RETURN_ACCELERATION = b.comment("How strongly a returning ball is pulled towards the thrower each tick.")
                .defineInRange("returnAcceleration", 0.15, 0.01, 2.0);
        BALL_DAMAGED_CHANCE_ON_BLOCK_HIT = b.comment("Chance (0..1) for the ball to become damaged when it hits a block. A damaged ball is imperfect and weaker.")
                .defineInRange("damagedChanceOnBlockHit", 0.05, 0.0, 1.0);
        BALL_DAMAGED_MULTIPLIER = b.comment("Damage multiplier of a damaged (imperfect) ball.")
                .defineInRange("damagedDamageMultiplier", 0.7, 0.0, 1.0);
        BALL_RICOCHET_MAX_BOUNCES = b.comment("A spinning ball ricochets off blocks this many times before it starts returning (0 = no ricochet).")
                .defineInRange("ricochetMaxBounces", 2, 0, 16);
        BALL_RICOCHET_SPEED_RETENTION = b.comment("Share of the speed kept after a ricochet.")
                .defineInRange("ricochetSpeedRetention", 0.7, 0.05, 1.0);
        BALL_RICOCHET_MIN_SPEED = b.comment("Below this speed (blocks per tick) after a ricochet the ball stops bouncing and returns.")
                .defineInRange("ricochetMinSpeed", 0.4, 0.0, 10.0);
        BALL_REPAIR_MATERIAL = b.comment("Anvil: a damaged steel ball + this item (one per ball) restores the perfect sphere.")
                .define("repairMaterial", "minecraft:iron_ingot");
        BALL_REPAIR_LEVEL_COST = b.comment("Experience levels the anvil repair costs.")
                .defineInRange("repairLevelCost", 1, 0, 39);
        b.pop();

        b.push("holster");
        HOLSTER_CAPACITY = b.comment("How many steel balls the holster holds (Gyro's belt has two side holsters).")
                .defineInRange("capacity", 2, 1, 16);
        b.pop();

        b.push("compat");
        b.push("curios");
        COMPAT_CURIOS_ENABLED = b.comment("If Curios is installed, a holster worn in the belt slot is used first. No effect without Curios.")
                .define("enabled", true);
        b.pop(2);

        SPEC = b.build();
    }
}
