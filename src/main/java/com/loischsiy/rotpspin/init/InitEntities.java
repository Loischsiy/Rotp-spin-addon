package com.loischsiy.rotpspin.init;

import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.entity.GyroTeacherEntity;
import com.loischsiy.rotpspin.entity.SpunBlockEntity;
import com.loischsiy.rotpspin.entity.SpunItemEntity;
import com.loischsiy.rotpspin.entity.SteelBallEntity;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class InitEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(
            ForgeRegistries.ENTITIES, AddonMain.MOD_ID);

    public static final RegistryObject<EntityType<SteelBallEntity>> STEEL_BALL = ENTITIES.register("steel_ball",
            () -> EntityType.Builder.<SteelBallEntity>of(SteelBallEntity::new, EntityClassification.MISC)
            .sized(0.25F, 0.25F)
            .clientTrackingRange(4)
            .updateInterval(20)
            .build(AddonMain.MOD_ID + ":steel_ball"));

    public static final RegistryObject<EntityType<SpunItemEntity>> SPUN_ITEM = ENTITIES.register("spun_item",
            () -> EntityType.Builder.<SpunItemEntity>of(SpunItemEntity::new, EntityClassification.MISC)
            .sized(0.25F, 0.25F)
            .clientTrackingRange(4)
            .updateInterval(20)
            .build(AddonMain.MOD_ID + ":spun_item"));

    public static final RegistryObject<EntityType<SpunBlockEntity>> SPUN_BLOCK = ENTITIES.register("spun_block",
            () -> EntityType.Builder.<SpunBlockEntity>of(SpunBlockEntity::new, EntityClassification.MISC)
            .sized(0.98F, 0.98F)
            .clientTrackingRange(4)
            .updateInterval(20)
            .build(AddonMain.MOD_ID + ":spun_block"));

    public static final RegistryObject<EntityType<GyroTeacherEntity>> GYRO_TEACHER = ENTITIES.register("gyro_teacher",
            () -> EntityType.Builder.<GyroTeacherEntity>of(GyroTeacherEntity::new, EntityClassification.CREATURE)
            .sized(0.6F, 1.95F)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(AddonMain.MOD_ID + ":gyro_teacher"));
}
