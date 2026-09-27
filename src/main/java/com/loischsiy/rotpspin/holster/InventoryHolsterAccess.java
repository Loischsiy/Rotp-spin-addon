package com.loischsiy.rotpspin.holster;

import java.util.function.Predicate;

import com.loischsiy.rotpspin.item.GyrosHolsterItem;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

/** Holster anywhere in the player's inventory (hotbar, main inventory, armor or offhand slots). */
public class InventoryHolsterAccess implements IHolsterAccess {
    public static final InventoryHolsterAccess INSTANCE = new InventoryHolsterAccess();

    private InventoryHolsterAccess() {}

    @Override
    public ItemStack findHolster(PlayerEntity player, Predicate<ItemStack> filter) {
        PlayerInventory inventory = player.inventory;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.getItem() instanceof GyrosHolsterItem && filter.test(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
