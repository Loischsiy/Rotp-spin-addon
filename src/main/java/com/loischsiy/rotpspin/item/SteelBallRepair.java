package com.loischsiy.rotpspin.item;

import javax.annotation.Nullable;

import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.config.SpinConfig;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Anvil: damaged steel balls + the repair material (one per ball) = perfect spheres again.
 * The damaged-ball penalty (docs/spin-lore.md, Ball Breaker) stays meaningful: a repair costs
 * metal and experience and needs an anvil, not a crafting grid.
 */
@EventBusSubscriber(modid = AddonMain.MOD_ID)
public class SteelBallRepair {

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        if (!(left.getItem() instanceof SteelBallItem) || !SteelBallItem.isChipped(left)) {
            return;
        }
        Item material = repairMaterial();
        if (material == null || right.getItem() != material || right.getCount() < left.getCount()) {
            return;
        }
        ItemStack repaired = left.copy();
        SteelBallItem.setChipped(repaired, false);
        event.setOutput(repaired);
        event.setMaterialCost(left.getCount());
        event.setCost(SpinConfig.BALL_REPAIR_LEVEL_COST.get());
    }

    @Nullable
    static Item repairMaterial() {
        ResourceLocation id = ResourceLocation.tryParse(SpinConfig.BALL_REPAIR_MATERIAL.get());
        return id != null && ForgeRegistries.ITEMS.containsKey(id) ? ForgeRegistries.ITEMS.getValue(id) : null;
    }
}
