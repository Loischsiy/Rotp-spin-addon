package com.loischsiy.rotpspin.config;

import java.util.Arrays;
import java.util.List;

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

    // Lesson 1: Spin on one's own body
    public static final ForgeConfigSpec.IntValue BRACE_WINDUP_TICKS;
    public static final ForgeConfigSpec.DoubleValue BRACE_ENERGY_PER_TICK;
    public static final ForgeConfigSpec.DoubleValue BRACE_DAMAGE_REDUCTION;
    public static final ForgeConfigSpec.DoubleValue BRACE_ENERGY_PER_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue BRACE_ATTACKER_KNOCKBACK;
    public static final ForgeConfigSpec.BooleanValue BRACE_NO_KNOCKBACK;
    public static final ForgeConfigSpec.IntValue BRACE_SLOWNESS_AMPLIFIER;
    public static final ForgeConfigSpec.IntValue BRACE_COOLDOWN_TICKS;

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
    public static final ForgeConfigSpec.DoubleValue BLOCK_SPIN_ENERGY_COST;
    public static final ForgeConfigSpec.DoubleValue BLOCK_SPIN_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue BLOCK_SPIN_VELOCITY;
    public static final ForgeConfigSpec.IntValue BLOCK_SPIN_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.DoubleValue BLOCK_SPIN_REACH;

    // Lesson progression
    public static final ForgeConfigSpec.BooleanValue LESSONS_ENABLED;
    public static final ForgeConfigSpec.IntValue LESSON2_BALL_HITS;
    public static final ForgeConfigSpec.IntValue LESSON3_HIJACKS;
    public static final ForgeConfigSpec.IntValue LESSON4_GOLDEN_HITS;
    public static final ForgeConfigSpec.IntValue LESSON5_GOLDEN_HITS;

    // Golden Spin (lesson 4) and Super Spin (lesson 5)
    public static final ForgeConfigSpec.DoubleValue GOLDEN_MULT_4;
    public static final ForgeConfigSpec.DoubleValue GOLDEN_MULT_5;
    public static final ForgeConfigSpec.DoubleValue GOLDEN_CHIPPED_RETENTION;
    public static final ForgeConfigSpec.DoubleValue GOLDEN_HORSE_GALLOP_SPEED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> GOLDEN_DEAD_CATEGORIES;
    public static final ForgeConfigSpec.BooleanValue GOLDEN_SNOWFALL_CALIBRATES;
    public static final ForgeConfigSpec.IntValue HAND_FRAME_HOLD_TICKS;
    public static final ForgeConfigSpec.IntValue HAND_FRAME_DURATION_TICKS;
    public static final ForgeConfigSpec.DoubleValue HAND_FRAME_ENERGY_PER_TICK;
    public static final ForgeConfigSpec.IntValue HAND_FRAME_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.DoubleValue SUPER_SPIN_MULT;
    public static final ForgeConfigSpec.IntValue SUPER_SPIN_GALLOP_TICKS;
    public static final ForgeConfigSpec.IntValue SUPER_SPIN_GRACE_TICKS;
    public static final ForgeConfigSpec.DoubleValue SUPER_SPIN_CRASH_STOP_FRACTION;
    public static final ForgeConfigSpec.DoubleValue SUPER_SPIN_HORSE_MIN_HEALTH;
    public static final ForgeConfigSpec.BooleanValue SUPER_SPIN_DETOUR_ENABLED;
    public static final ForgeConfigSpec.DoubleValue SUPER_SPIN_DETOUR_RANGE;
    public static final ForgeConfigSpec.IntValue SUPER_SPIN_DETOUR_DURATION_TICKS;
    public static final ForgeConfigSpec.DoubleValue SUPER_SPIN_DETOUR_KICK_DAMAGE;

    public static final ForgeConfigSpec.DoubleValue HEALING_ENERGY_PER_TICK;
    public static final ForgeConfigSpec.IntValue HEALING_INTERVAL_TICKS;
    public static final ForgeConfigSpec.DoubleValue HEALING_AMOUNT;
    public static final ForgeConfigSpec.DoubleValue HEALING_TARGET_REACH;
    public static final ForgeConfigSpec.BooleanValue HEALING_CURES_HARMFUL;
    public static final ForgeConfigSpec.IntValue HEALING_XRAY_WATER_RADIUS;
    public static final ForgeConfigSpec.DoubleValue HEALING_XRAY_AMOUNT_MULT;

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
    public static final ForgeConfigSpec.BooleanValue FRICTION_BARK_STRIPPING;
    public static final ForgeConfigSpec.DoubleValue FRICTION_BARK_MIN_SPEED;
    public static final ForgeConfigSpec.DoubleValue FRICTION_BARK_SPEED_RETENTION;
    public static final ForgeConfigSpec.BooleanValue FRICTION_BULLET_CUTTING;
    public static final ForgeConfigSpec.DoubleValue FRICTION_BULLET_MIN_SPEED;
    public static final ForgeConfigSpec.IntValue FRICTION_BULLETS_PER_CUT;
    public static final ForgeConfigSpec.IntValue FRICTION_BULLET_NUGGETS_PER_BLOCK;
    public static final ForgeConfigSpec.DoubleValue FRICTION_BULLET_SPEED_RETENTION;
    public static final ForgeConfigSpec.BooleanValue ROPE_ENABLED;
    public static final ForgeConfigSpec.IntValue ROPE_MAX_TICKS;
    public static final ForgeConfigSpec.DoubleValue ROPE_PULL_STRENGTH;
    public static final ForgeConfigSpec.DoubleValue ROPE_MAX_SPEED;
    public static final ForgeConfigSpec.DoubleValue ROPE_RELEASE_DISTANCE;
    public static final ForgeConfigSpec.DoubleValue ROPE_MAX_LENGTH;
    public static final ForgeConfigSpec.ConfigValue<String> BALL_REPAIR_MATERIAL;
    public static final ForgeConfigSpec.IntValue BALL_REPAIR_LEVEL_COST;

    // Gyro's holster
    public static final ForgeConfigSpec.IntValue HOLSTER_CAPACITY;

    // Ball Breaker (the ultimate throw's visualization)
    public static final ForgeConfigSpec.DoubleValue BALL_BREAKER_TOUCH_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue BALL_BREAKER_TOUCH_RANGE;
    public static final ForgeConfigSpec.DoubleValue BALL_BREAKER_TOUCH_STAMINA;
    public static final ForgeConfigSpec.IntValue BALL_BREAKER_SENESCENCE_DURATION;
    public static final ForgeConfigSpec.IntValue BALL_BREAKER_SENESCENCE_INTERVAL;
    public static final ForgeConfigSpec.DoubleValue BALL_BREAKER_SENESCENCE_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue BALL_BREAKER_SPIN_DAMAGE_MULT;

    // Spin resonance (a spinning object amplifies other throws, SBR ch. 23)
    public static final ForgeConfigSpec.BooleanValue RESONANCE_ENABLED;
    public static final ForgeConfigSpec.DoubleValue RESONANCE_RADIUS;
    public static final ForgeConfigSpec.DoubleValue RESONANCE_DAMAGE_PER_SOURCE;
    public static final ForgeConfigSpec.DoubleValue RESONANCE_MAX_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue RESONANCE_MAX_SOURCES;
    public static final ForgeConfigSpec.IntValue RESONANCE_EXTRA_FLIGHT_TICKS_PER_SOURCE;

    // Wrecking Ball (royal guard version of the steel ball)
    public static final ForgeConfigSpec.IntValue WRECKING_SATELLITES;
    public static final ForgeConfigSpec.IntValue WRECKING_RELEASE_AFTER_TICKS;
    public static final ForgeConfigSpec.DoubleValue WRECKING_SATELLITE_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue WRECKING_SATELLITE_SPEED;
    public static final ForgeConfigSpec.DoubleValue WRECKING_SATELLITE_RANGE;
    public static final ForgeConfigSpec.IntValue WRECKING_SATELLITE_LIFETIME;
    public static final ForgeConfigSpec.DoubleValue WRECKING_SHOCKWAVE_RADIUS;
    public static final ForgeConfigSpec.DoubleValue WRECKING_SHOCKWAVE_DAMAGE;
    public static final ForgeConfigSpec.IntValue WRECKING_NEGLECT_DURATION;

    // Optional integrations
    public static final ForgeConfigSpec.BooleanValue COMPAT_CURIOS_ENABLED;

    // Cloak as a sail (SBR ch. 11)
    public static final ForgeConfigSpec.BooleanValue SAIL_ENABLED;
    public static final ForgeConfigSpec.DoubleValue SAIL_MIN_FALL_DISTANCE;
    public static final ForgeConfigSpec.DoubleValue SAIL_MAX_FALL_SPEED;
    public static final ForgeConfigSpec.DoubleValue SAIL_FORWARD_PUSH;
    public static final ForgeConfigSpec.DoubleValue SAIL_MAX_HORIZONTAL_SPEED;
    public static final ForgeConfigSpec.DoubleValue SAIL_COST_PER_TICK;
    public static final ForgeConfigSpec.DoubleValue SAIL_START_ENERGY;
    public static final ForgeConfigSpec.BooleanValue COMPAT_TUSK_ENABLED;
    public static final ForgeConfigSpec.IntValue COMPAT_TUSK_CHARGE_4;
    public static final ForgeConfigSpec.IntValue COMPAT_TUSK_CHARGE_5;
    public static final ForgeConfigSpec.BooleanValue COMPAT_TUSK_HERBS_ENABLED;
    public static final ForgeConfigSpec.IntValue COMPAT_TUSK_HERB_DURATION;
    public static final ForgeConfigSpec.IntValue COMPAT_TUSK_HERB_INTERVAL;
    public static final ForgeConfigSpec.IntValue COMPAT_TUSK_HERB_MAX_STACK;

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

        b.push("body_brace");
        BRACE_WINDUP_TICKS = b.comment("Ability \"Spin Body Brace\" (hold, lesson 1): ticks of holding before the spinning body becomes rigid (Gyro withstood a bullet and a bomb blast, SBR ch. 22, 25, 54).")
                .defineInRange("windupTicks", 5, 0, 200);
        BRACE_ENERGY_PER_TICK = b.comment("Spin energy consumed per tick while holding the stance.")
                .defineInRange("energyPerTick", 0.4, 0.0, 1000.0);
        BRACE_DAMAGE_REDUCTION = b.comment("Share of a kinetic blow (projectile, explosion, melee) the rigid body passes on. Capped at 0.95 in code: temporary toughness, not invulnerability.")
                .defineInRange("damageReduction", 0.7, 0.0, 0.95);
        BRACE_ENERGY_PER_DAMAGE = b.comment("Spin energy spent per absorbed damage point; with too little energy only part of the blow is absorbed.")
                .defineInRange("energyPerAbsorbedDamage", 4.0, 0.0, 1000.0);
        BRACE_ATTACKER_KNOCKBACK = b.comment("Knockback strength a melee attacker receives: the energy of the blow is passed back (Wekapipo, ch. 54). 0 disables.")
                .defineInRange("attackerKnockback", 1.0, 0.0, 5.0);
        BRACE_NO_KNOCKBACK = b.comment("The rigid body is not knocked back while the stance is held (Gyro stood his ground against a bullet and a blast).")
                .define("noOwnKnockback", true);
        BRACE_SLOWNESS_AMPLIFIER = b.comment("Slowness amplifier while the body is rigid (-1 disables).")
                .defineInRange("slownessAmplifier", 1, -1, 5);
        BRACE_COOLDOWN_TICKS = b.comment("Cooldown after releasing a completed stance, in ticks.")
                .defineInRange("cooldownTicks", 40, 0, 1200);
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

        b.push("block_spin");
        BLOCK_SPIN_ENERGY_COST = b.comment("Lesson 3 \"Believe in the rotation\" for blocks: Spin energy consumed by ripping out and throwing the aimed block.")
                .defineInRange("energyCost", 15.0, 0.0, 10000.0);
        BLOCK_SPIN_DAMAGE = b.comment("Base damage of a spinning block (multiplied by its velocity, like an arrow). Crude matter gets no Golden bonus.")
                .defineInRange("damage", 3.0, 0.0, 1000.0);
        BLOCK_SPIN_VELOCITY = b.comment("Launch velocity of a spinning block (blocks per tick).")
                .defineInRange("velocity", 1.4, 0.1, 10.0);
        BLOCK_SPIN_COOLDOWN_TICKS = b.comment("Cooldown of the technique, in ticks.")
                .defineInRange("cooldownTicks", 20, 0, 1200);
        BLOCK_SPIN_REACH = b.comment("How far (blocks) the aimed block may be to be ripped out.")
                .defineInRange("reach", 6.0, 1.0, 32.0);
        b.pop();

        b.push("lessons");
        LESSONS_ENABLED = b.comment("Gyro's lessons unlock the techniques step by step. false = everything is available at once.")
                .define("enabled", true);
        LESSON2_BALL_HITS = b.comment("Lesson 2 \"Use your muscles\" is learned after this many hits on creatures with a spinning steel ball.")
                .defineInRange("lesson2BallHits", 10, 0, 10000);
        LESSON3_HIJACKS = b.comment("Lesson 3 \"Believe in the rotation\" is learned after this many successful Muscle Hijacks.")
                .defineInRange("lesson3Hijacks", 5, 0, 10000);
        LESSON4_GOLDEN_HITS = b.comment("Lesson 4 \"Pay tribute. Spin the bullets in the golden ratio\" is learned after this many hits on creatures with a spinning steel ball at lesson 3.")
                .defineInRange("lesson4GoldenHits", 15, 0, 10000);
        LESSON5_GOLDEN_HITS = b.comment("Lesson 5 \"The shortest route is the detour\" (Super Spin) is learned after this many spinning ball hits in total at lesson 4+ (cumulative with the lesson 4 counter).")
                .defineInRange("lesson5GoldenHits", 30, 0, 10000);
        b.pop();

        b.push("golden_spin");
        GOLDEN_MULT_4 = b.comment("Lesson 4 Golden Spin: damage multiplier of a spinning steel ball while calibrated (living biome, falling snow or calibration buckle in the inventory).")
                .defineInRange("multiplier4", 1.5, 1.0, 100.0);
        GOLDEN_MULT_5 = b.comment("Lesson 5 Super Spin: damage multiplier of a spinning steel ball, everywhere, no calibration needed.")
                .defineInRange("multiplier5", 2.0, 1.0, 100.0);
        GOLDEN_CHIPPED_RETENTION = b.comment("A chipped (imperfect) ball keeps only this share of the Golden bonus above x1 (Ball Breaker was incomplete with a damaged ball).")
                .defineInRange("chippedRetention", 0.5, 0.0, 1.0);
        GOLDEN_HORSE_GALLOP_SPEED = b.comment("Lesson 5 \"The shortest route is the detour\": a ridden horse moving at least this fast (blocks per tick, horizontal, smoothed over a few ticks) gives Super Spin without calibration, from lesson 4.")
                .defineInRange("horseGallopSpeed", 0.2, 0.0, 5.0);
        GOLDEN_DEAD_CATEGORIES = b.comment("Biome categories with no natural golden-ratio markers (frozen strait, desert, void): Golden Spin needs the calibration buckle there. Names of Biome.Category.")
                .defineList("deadBiomeCategories", Arrays.asList("NETHER", "THEEND", "ICY", "DESERT", "NONE"),
                        entry -> entry instanceof String);
        GOLDEN_SNOWFALL_CALIBRATES = b.comment("Falling snow on the thrower (open sky, snowy weather) is a golden-ratio reference even in a dead biome: snowflakes saved Gyro on the frozen strait (SBR ch. 54).")
                .define("snowfallCalibrates", true);
        b.push("hand_frame");
        HAND_FRAME_HOLD_TICKS = b.comment("Ability \"Golden Rectangle\" (hold, lesson 4, empty hands): ticks of holding needed to frame the golden rectangle with both hands. A hit breaks the framing (Wekapipo struck Gyro's hands, SBR ch. 51-54).")
                .defineInRange("holdTicks", 30, 1, 1200);
        HAND_FRAME_DURATION_TICKS = b.comment("How long the framed rectangle calibrates Golden Spin, in ticks (anywhere, including dead biomes).")
                .defineInRange("durationTicks", 600, 1, 72000);
        HAND_FRAME_ENERGY_PER_TICK = b.comment("Spin energy consumed per tick while framing.")
                .defineInRange("energyPerTick", 0.5, 0.0, 1000.0);
        HAND_FRAME_COOLDOWN_TICKS = b.comment("Cooldown after a completed framing, in ticks.")
                .defineInRange("cooldownTicks", 400, 0, 72000);
        b.pop();
        b.push("super_spin");
        SUPER_SPIN_MULT = b.comment("Super Spin (natural gallop or the lesson 5 detour): damage multiplier of a spinning steel ball. Never lowers the multiplier the user already has.")
                .defineInRange("multiplier", 3.0, 1.0, 100.0);
        SUPER_SPIN_GALLOP_TICKS = b.comment("Ticks of gallop (see horseGallopSpeed) before the horse's energy flows into the throw. A crash into an obstacle, a hit on the horse or on the rider resets it (SBR ch. 80, 85); fall damage from rough ground does not.")
                .defineInRange("gallopTicks", 60, 1, 12000);
        SUPER_SPIN_GRACE_TICKS = b.comment("The horse may drop below gallop speed for this many ticks in a row (a hill, a turn, a jump, a lag spike) without losing the build-up. It does not grow meanwhile.")
                .defineInRange("graceTicks", 20, 0, 1200);
        SUPER_SPIN_CRASH_STOP_FRACTION = b.comment("A collision is a crash only if the horse's speed in that tick falls below this share of its gallop speed (a real stop against an obstacle). Sliding along a wall or stepping up a block is not a crash. 0 disables crashes.")
                .defineInRange("crashStopFraction", 0.35, 0.0, 1.0);
        SUPER_SPIN_HORSE_MIN_HEALTH = b.comment("The horse must be healthy: its health share must be at least this.")
                .defineInRange("horseMinHealth", 0.5, 0.0, 1.0);
        SUPER_SPIN_DETOUR_ENABLED = b.comment("Lesson 5 \"The shortest route is the detour\" (SBR ch. 85): a spinning ball hitting your own horse makes it kick you and hands over Super Spin. A chipped ball fails.")
                .define("detourEnabled", true);
        SUPER_SPIN_DETOUR_RANGE = b.comment("The user must be this close to the horse (blocks) to be kicked.")
                .defineInRange("detourRange", 4.0, 0.5, 32.0);
        SUPER_SPIN_DETOUR_DURATION_TICKS = b.comment("How long the energy of the kick stays in the user, in ticks.")
                .defineInRange("detourDurationTicks", 200, 1, 72000);
        SUPER_SPIN_DETOUR_KICK_DAMAGE = b.comment("Damage of the hoof kick to the user.")
                .defineInRange("detourKickDamage", 2.0, 0.0, 100.0);
        b.pop();
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
        HEALING_XRAY_WATER_RADIUS = b.comment("\"X-ray\": water (a pool or a filled cauldron) within this many blocks of the patient shows the ripples of the rotation: the healer sees the hidden ailments and the treatment is more precise. 0 disables.")
                .defineInRange("xrayWaterRadius", 2, 0, 8);
        HEALING_XRAY_AMOUNT_MULT = b.comment("\"X-ray\": health restored per interval is multiplied by this while the water shows the ripples.")
                .defineInRange("xrayAmountMultiplier", 1.5, 1.0, 10.0);
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
        b.push("friction");
        FRICTION_BARK_STRIPPING = b.comment("A spinning steel ball strips the bark off a log it hits, like an axe (SBR ch. 30). Needs the thrower's permission to use items at that spot.")
                .define("barkStripping", true);
        FRICTION_BARK_MIN_SPEED = b.comment("Minimum ball speed (blocks per tick) to strip bark.")
                .defineInRange("barkMinSpeed", 0.8, 0.0, 10.0);
        FRICTION_BARK_SPEED_RETENTION = b.comment("Share of speed the ball keeps after stripping bark (friction against the trunk).")
                .defineInRange("barkSpeedRetention", 0.7, 0.0, 1.0);
        FRICTION_BULLET_CUTTING = b.comment("A spinning steel ball cuts bullets (iron nuggets) out of an iron block it hits (SBR ch. 44), once per flight. Needs the thrower's permission to use items at that spot.")
                .define("bulletCutting", true);
        FRICTION_BULLET_MIN_SPEED = b.comment("Minimum ball speed (blocks per tick) to cut bullets in metal.")
                .defineInRange("bulletMinSpeed", 1.0, 0.0, 10.0);
        FRICTION_BULLETS_PER_CUT = b.comment("Iron nuggets cut out per hit.")
                .defineInRange("bulletsPerCut", 3, 1, 64);
        FRICTION_BULLET_NUGGETS_PER_BLOCK = b.comment("Nuggets an iron block is worth: the block is used up with chance bulletsPerCut / this, so carving never yields more iron than crafting.")
                .defineInRange("bulletNuggetsPerBlock", 81, 1, 1000);
        FRICTION_BULLET_SPEED_RETENTION = b.comment("Share of speed the ball keeps after cutting the metal.")
                .defineInRange("bulletSpeedRetention", 0.5, 0.0, 1.0);
        b.pop();
        b.push("rope");
        ROPE_ENABLED = b.comment("Sneak + throw a spinning ball: it weaves a rope (SBR ch. 55), anchors in the first block it hits and pulls the thrower to it.")
                .define("enabled", true);
        ROPE_MAX_TICKS = b.comment("Longest pull in ticks, then the rope lets go and the ball returns.")
                .defineInRange("maxTicks", 60, 1, 1200);
        ROPE_PULL_STRENGTH = b.comment("Velocity added to the thrower towards the anchor each tick (blocks per tick).")
                .defineInRange("pullStrength", 0.25, 0.0, 5.0);
        ROPE_MAX_SPEED = b.comment("Speed cap of the pulled thrower (blocks per tick).")
                .defineInRange("maxSpeed", 1.2, 0.0, 10.0);
        ROPE_RELEASE_DISTANCE = b.comment("The rope lets go when the thrower is this close to the anchor (blocks).")
                .defineInRange("releaseDistance", 2.0, 0.0, 64.0);
        ROPE_MAX_LENGTH = b.comment("The ball anchors only this close to the thrower (blocks); farther it just returns.")
                .defineInRange("maxLength", 32.0, 1.0, 256.0);
        b.pop();
        b.pop();

        b.push("holster");
        HOLSTER_CAPACITY = b.comment("How many steel balls the holster holds (Gyro's belt has two side holsters).")
                .defineInRange("capacity", 2, 1, 16);
        b.pop();

        b.push("ball_breaker");
        BALL_BREAKER_TOUCH_DAMAGE = b.comment("Senescence Touch: direct armor-piercing damage in the touched zone.")
                .defineInRange("touchDamage", 6.0, 0.0, 1000.0);
        BALL_BREAKER_TOUCH_RANGE = b.comment("Senescence Touch: radius around the Stand in which everything ages.")
                .defineInRange("touchRange", 3.0, 0.5, 16.0);
        BALL_BREAKER_TOUCH_STAMINA = b.comment("Senescence Touch: stamina cost.")
                .defineInRange("touchStamina", 30.0, 0.0, 1000.0);
        BALL_BREAKER_SENESCENCE_DURATION = b.comment("How long (ticks) the aging lasts after a touch.")
                .defineInRange("senescenceDurationTicks", 120, 10, 6000);
        BALL_BREAKER_SENESCENCE_INTERVAL = b.comment("Aging wounds once per this many ticks.")
                .defineInRange("senescenceIntervalTicks", 20, 1, 1200);
        BALL_BREAKER_SENESCENCE_DAMAGE = b.comment("Aging damage per interval (ignores armor, like the canon bypass).")
                .defineInRange("senescenceDamage", 1.0, 0.0, 100.0);
        BALL_BREAKER_SPIN_DAMAGE_MULT = b.comment("Ball Breaker summoned + Golden Spin calibrated: damage multiplier of the thrower's spinning steel balls (the visualization amplifies the Spin itself).")
                .defineInRange("spinDamageMultiplier", 1.5, 1.0, 100.0);
        b.pop();

        b.push("wrecking_ball");
        WRECKING_SATELLITES = b.comment("How many satellite balls a Wrecking Ball carries (canon: 14; the model always shows 14 sockets).")
                .defineInRange("satellites", 14, 0, 14);
        WRECKING_RELEASE_AFTER_TICKS = b.comment("Ticks of flight before the satellites fly out.")
                .defineInRange("releaseAfterTicks", 8, 1, 200);
        WRECKING_SATELLITE_DAMAGE = b.comment("Base damage of one satellite (like an arrow, multiplied by its velocity).")
                .defineInRange("satelliteDamage", 1.0, 0.0, 1000.0);
        WRECKING_SATELLITE_SPEED = b.comment("Launch velocity of a satellite (blocks per tick).")
                .defineInRange("satelliteSpeed", 1.2, 0.1, 10.0);
        WRECKING_SATELLITE_RANGE = b.comment("The satellites aim at the nearest living victim within this distance; with none near, they fan out.")
                .defineInRange("satelliteRange", 8.0, 1.0, 64.0);
        WRECKING_SATELLITE_LIFETIME = b.comment("Ticks before a spent satellite discards itself.")
                .defineInRange("satelliteLifetime", 60, 10, 1200);
        WRECKING_SHOCKWAVE_RADIUS = b.comment("Even a miss raises a shockwave: living victims within this radius are wounded and disoriented.")
                .defineInRange("shockwaveRadius", 4.0, 0.0, 32.0);
        WRECKING_SHOCKWAVE_DAMAGE = b.comment("Base damage of the miss shockwave.")
                .defineInRange("shockwaveDamage", 3.0, 0.0, 1000.0);
        WRECKING_NEGLECT_DURATION = b.comment("How long (ticks) the shockwave's hemispatial neglect lasts: victims ignore their left side.")
                .defineInRange("neglectDurationTicks", 100, 10, 6000);
        b.pop();

        b.push("resonance");
        RESONANCE_ENABLED = b.comment("Spin resonance (SBR ch. 23): spinning projectiles near the thrower amplify a new spinning steel ball throw and extend its range.")
                .define("enabled", true);
        RESONANCE_RADIUS = b.comment("Spinning projectiles (steel balls, spun items and blocks, any owner; satellites excluded) within this distance of the thrower count as sources.")
                .defineInRange("radius", 8.0, 0.0, 64.0);
        RESONANCE_DAMAGE_PER_SOURCE = b.comment("Damage bonus per source: multiplier = 1 + sources * this.")
                .defineInRange("damagePerSource", 0.15, 0.0, 10.0);
        RESONANCE_MAX_MULTIPLIER = b.comment("Upper limit of the resonance damage multiplier.")
                .defineInRange("maxMultiplier", 1.5, 1.0, 100.0);
        RESONANCE_MAX_SOURCES = b.comment("At most this many sources count.")
                .defineInRange("maxSources", 3, 0, 64);
        RESONANCE_EXTRA_FLIGHT_TICKS_PER_SOURCE = b.comment("Extra ticks of forward flight per source before the ball turns back (longer range).")
                .defineInRange("extraFlightTicksPerSource", 5, 0, 200);
        b.pop();

        b.push("cloak_sail");
        SAIL_ENABLED = b.comment("A Spin user falling with a steel ball in hand holds a cloak as a sail (SBR ch. 11): slow descent, a glide forward, no fall damage. The cloak is any item in tag rotp_spin:spin_sails (default: elytra) in the chest slot or, with Curios, in any curio slot.")
                .define("enabled", true);
        SAIL_MIN_FALL_DISTANCE = b.comment("Blocks of free fall before the sail opens (an ordinary jump costs nothing).")
                .defineInRange("minFallDistance", 1.5, 0.0, 64.0);
        SAIL_MAX_FALL_SPEED = b.comment("Descent speed cap under the sail (blocks per tick).")
                .defineInRange("maxFallSpeed", 0.12, 0.0, 4.0);
        SAIL_FORWARD_PUSH = b.comment("Horizontal push along the look direction each tick (blocks per tick).")
                .defineInRange("forwardPush", 0.03, 0.0, 1.0);
        SAIL_MAX_HORIZONTAL_SPEED = b.comment("Horizontal speed cap under the sail (blocks per tick).")
                .defineInRange("maxHorizontalSpeed", 0.5, 0.0, 4.0);
        SAIL_COST_PER_TICK = b.comment("Spin energy spent per tick of sailing (regeneration keeps running).")
                .defineInRange("costPerTick", 0.75, 0.0, 100.0);
        SAIL_START_ENERGY = b.comment("Spin energy needed to open the sail (keeping it open needs only costPerTick), so an exhausted user falls instead of flickering.")
                .defineInRange("startEnergy", 10.0, 0.0, 100.0);
        b.pop();

        b.push("compat");
        b.push("curios");
        COMPAT_CURIOS_ENABLED = b.comment("If Curios is installed, a holster worn in the belt slot is used first. No effect without Curios.")
                .define("enabled", true);
        b.pop();
        b.push("tusk");
        COMPAT_TUSK_ENABLED = b.comment("If the Tusk stand addon (rotp_t) is installed, a calibrated Golden Spin charges the nails with rotation. No effect without rotp_t.")
                .define("enabled", true);
        COMPAT_TUSK_CHARGE_4 = b.comment("Lesson 4 Golden Spin: spin charge added to Tusk nails (+damage, opens Tusk's own wormhole on break).")
                .defineInRange("charge4", 2, 0, 20);
        COMPAT_TUSK_CHARGE_5 = b.comment("Lesson 5 Super Spin: spin charge added to Tusk nails.")
                .defineInRange("charge5", 4, 0, 20);
        COMPAT_TUSK_HERBS_ENABLED = b.comment("Tusk user chewing a herb from the rotp_spin:tusk_nail_herbs tag (mint and chamomile, SBR ch. 45; default: oxeye daisy) gets a herbal infusion that grows extra nails.")
                .define("herbsEnabled", true);
        COMPAT_TUSK_HERB_DURATION = b.comment("Herbal infusion duration per herb, ticks.")
                .defineInRange("herbDurationTicks", 1200, 20, 24000);
        COMPAT_TUSK_HERB_INTERVAL = b.comment("While the infusion lasts, one extra nail grows every this many ticks (on top of Tusk's own regrowth).")
                .defineInRange("herbIntervalTicks", 100, 1, 6000);
        COMPAT_TUSK_HERB_MAX_STACK = b.comment("Infusion can be stacked up to this many herb durations.")
                .defineInRange("herbMaxStack", 3, 1, 20);
        b.pop();
        b.pop();

        SPEC = b.build();
    }
}
