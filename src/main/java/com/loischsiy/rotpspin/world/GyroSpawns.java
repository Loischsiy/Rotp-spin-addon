package com.loischsiy.rotpspin.world;

import java.util.Random;

import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.entity.GyroTeacherEntity;
import com.loischsiy.rotpspin.init.InitEntities;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.horse.HorseEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.WorldEntitySpawner;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * Where Gyro Zeppeli is met (docs/art/gyro_teacher.md, "Поведение NPC"):
 * alone in the prairies and the West the Steel Ball Run crossed, at a village on a race stage,
 * and with his saddled horse Valkyrie on a lead. All numbers live in {@link SpinConfig}.
 */
@EventBusSubscriber(modid = AddonMain.MOD_ID)
public final class GyroSpawns {
    /** Gyro is one man: never spawned in a group. */
    private static final int ALONE = 1;
    private static final float FULL_TURN = 360F;

    private GyroSpawns() {}

    @SubscribeEvent
    public static void onBiomeLoading(BiomeLoadingEvent event) {
        if (!SpinConfig.GYRO_NATURAL_SPAWN.get() || event.getCategory() == null
                || !GyroSpawnRules.categoryListed(event.getCategory().name(), SpinConfig.GYRO_BIOME_CATEGORIES.get())) {
            return;
        }
        event.getSpawns().addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(
                InitEntities.GYRO_TEACHER.get(), SpinConfig.GYRO_SPAWN_WEIGHT.get(), ALONE, ALONE));
    }

    /** Called from common setup (enqueueWork): ground placement and the natural spawn rules. */
    public static void registerPlacement() {
        EntitySpawnPlacementRegistry.register(InitEntities.GYRO_TEACHER.get(),
                EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, GyroSpawns::canSpawn);
    }

    private static boolean canSpawn(EntityType<GyroTeacherEntity> type, IServerWorld world,
            SpawnReason reason, BlockPos pos, Random random) {
        return world.getBlockState(pos.below()).isValidSpawn(world, pos.below(), type)
                && world.getRawBrightness(pos, 0) >= SpinConfig.GYRO_MIN_LIGHT.get()
                && !gyroNearby(world, pos, SpinConfig.GYRO_UNIQUE_RADIUS.get());
    }

    private static boolean gyroNearby(IServerWorld world, BlockPos pos, double radius) {
        return !world.getEntitiesOfClass(GyroTeacherEntity.class, new AxisAlignedBB(pos).inflate(radius)).isEmpty();
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.world instanceof ServerWorld)
                || !SpinConfig.GYRO_VILLAGE_SPAWN.get()) {
            return;
        }
        ServerWorld world = (ServerWorld) event.world;
        if (!GyroSpawnRules.isCheckTick(world.getGameTime(), SpinConfig.GYRO_VILLAGE_CHECK_INTERVAL.get())) {
            return;
        }
        for (ServerPlayerEntity player : world.players()) {
            if (!player.isSpectator()) {
                visitVillage(world, player.blockPosition());
            }
        }
    }

    private static void visitVillage(ServerWorld world, BlockPos playerPos) {
        StructureStart<?> village = world.structureFeatureManager().getStructureAt(playerPos, false, Structure.VILLAGE);
        if (village == null || !village.isValid()) {
            return;
        }
        BlockPos center = village.getLocatePos();
        if (!GyroSpawnRules.categoryListed(world.getBiome(center).getBiomeCategory().name(),
                SpinConfig.GYRO_VILLAGE_BIOME_CATEGORIES.get())
                || gyroNearby(world, center, SpinConfig.GYRO_VILLAGE_RADIUS.get())
                || !GyroSpawnRules.rolled(world.random.nextDouble(), SpinConfig.GYRO_VILLAGE_SPAWN_CHANCE.get())) {
            return;
        }
        EntityType<GyroTeacherEntity> type = InitEntities.GYRO_TEACHER.get();
        int spread = SpinConfig.GYRO_VILLAGE_SPAWN_SPREAD.get();
        for (int attempt = 0; attempt < SpinConfig.GYRO_VILLAGE_SPAWN_ATTEMPTS.get(); attempt++) {
            BlockPos column = center.offset(GyroSpawnRules.spreadOffset(world.random.nextDouble(), spread), 0,
                    GyroSpawnRules.spreadOffset(world.random.nextDouble(), spread));
            if (!world.hasChunkAt(column)) {
                continue;
            }
            BlockPos pos = world.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, column);
            if (!WorldEntitySpawner.isSpawnPositionOk(EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, world, pos, type)) {
                continue;
            }
            GyroTeacherEntity gyro = type.create(world);
            if (gyro == null) {
                return;
            }
            gyro.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, world.random.nextFloat() * FULL_TURN, 0);
            if (!world.noCollision(gyro)) {
                continue;
            }
            gyro.finalizeSpawn(world, world.getCurrentDifficultyAt(pos), SpawnReason.STRUCTURE, null, null);
            gyro.setPersistenceRequired();
            world.addFreshEntity(gyro);
            return;
        }
    }

    /** Valkyrie: saddled, on Gyro's lead, not tamed. Only for world spawns, never for eggs or commands. */
    public static void spawnHorse(IServerWorld world, GyroTeacherEntity gyro, SpawnReason reason) {
        if (!SpinConfig.GYRO_SPAWN_WITH_HORSE.get() || !(reason == SpawnReason.NATURAL
                || reason == SpawnReason.CHUNK_GENERATION || reason == SpawnReason.STRUCTURE)) {
            return;
        }
        HorseEntity horse = EntityType.HORSE.create(world.getLevel());
        if (horse == null) {
            return;
        }
        double offset = SpinConfig.GYRO_HORSE_OFFSET.get();
        horse.moveTo(gyro.getX() + offset, gyro.getY(), gyro.getZ(), gyro.yRot, 0);
        horse.finalizeSpawn(world, world.getCurrentDifficultyAt(horse.blockPosition()), reason, null, null);
        horse.equipSaddle(SoundCategory.NEUTRAL);
        horse.setLeashedTo(gyro, false);
        horse.setCustomName(new TranslationTextComponent("entity.rotp_spin.gyro_teacher.horse"));
        horse.setPersistenceRequired();
        world.addFreshEntity(horse);
    }
}
