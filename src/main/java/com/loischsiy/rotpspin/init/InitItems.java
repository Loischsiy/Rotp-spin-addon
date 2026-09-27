package com.loischsiy.rotpspin.init;

import com.github.standobyte.jojo.init.ModItems;
import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.item.GyrosHolsterItem;
import com.loischsiy.rotpspin.item.SteelBallItem;

import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class InitItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AddonMain.MOD_ID);

    public static final RegistryObject<SteelBallItem> STEEL_BALL = ITEMS.register("steel_ball",
            () -> new SteelBallItem(new Item.Properties().tab(ModItems.MAIN_TAB).stacksTo(1)));

    public static final RegistryObject<GyrosHolsterItem> GYROS_HOLSTER = ITEMS.register("gyros_holster",
            () -> new GyrosHolsterItem(new Item.Properties().tab(ModItems.MAIN_TAB).stacksTo(1)));
}
