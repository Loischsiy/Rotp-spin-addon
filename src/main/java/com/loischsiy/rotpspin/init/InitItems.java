package com.loischsiy.rotpspin.init;

import com.github.standobyte.jojo.init.ModItems;
import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.item.GyrosHolsterItem;
import com.loischsiy.rotpspin.item.SteelBallItem;
import com.loischsiy.rotpspin.item.WreckingBallItem;

import net.minecraft.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class InitItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AddonMain.MOD_ID);

    public static final RegistryObject<SteelBallItem> STEEL_BALL = ITEMS.register("steel_ball",
            () -> new SteelBallItem(new Item.Properties().tab(ModItems.MAIN_TAB).stacksTo(1)));

    public static final RegistryObject<WreckingBallItem> WRECKING_BALL = ITEMS.register("wrecking_ball",
            () -> new WreckingBallItem(new Item.Properties().tab(ModItems.MAIN_TAB).stacksTo(1)));

    public static final RegistryObject<GyrosHolsterItem> GYROS_HOLSTER = ITEMS.register("gyros_holster",
            () -> new GyrosHolsterItem(new Item.Properties().tab(ModItems.MAIN_TAB).stacksTo(1)));

    // Lesson 4: Gyro's belt buckle in golden-ratio proportions, the calibration reference.
    // A plain item on purpose: the whole behaviour is "in the inventory or not" (SteelBallEntity).
    public static final RegistryObject<Item> CALIBRATION_BUCKLE = ITEMS.register("calibration_buckle",
            () -> new Item(new Item.Properties().tab(ModItems.MAIN_TAB).stacksTo(1)));

    public static final RegistryObject<ForgeSpawnEggItem> GYRO_TEACHER_EGG = ITEMS.register("gyro_teacher_egg",
            () -> new ForgeSpawnEggItem(() -> InitEntities.GYRO_TEACHER.get(), 0x4A3626, 0xD8C27A,
                    new Item.Properties().tab(ModItems.MAIN_TAB)));
}
