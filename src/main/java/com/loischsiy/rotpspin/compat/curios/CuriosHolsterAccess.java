package com.loischsiy.rotpspin.compat.curios;

import java.util.function.Predicate;

import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.holster.IHolsterAccess;
import com.loischsiy.rotpspin.holster.InventoryHolsterAccess;
import com.loischsiy.rotpspin.item.GyrosHolsterItem;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

/**
 * Holster worn in a Curios slot (the "belt" one), falling back to the inventory.
 * The stack returned by Curios is the live slot content; Curios syncs its changes itself.
 * {@code compat.curios.enabled} is checked on every call, so it can be switched off without a restart.
 */
class CuriosHolsterAccess implements IHolsterAccess {

    @Override
    public ItemStack findHolster(PlayerEntity player, Predicate<ItemStack> filter) {
        if (SpinConfig.COMPAT_CURIOS_ENABLED.get()) {
            ItemStack worn = CuriosApi.getCuriosHelper()
                    .findFirstCurio(player, stack -> stack.getItem() instanceof GyrosHolsterItem && filter.test(stack))
                    .map(SlotResult::getStack)
                    .orElse(ItemStack.EMPTY);
            if (!worn.isEmpty()) {
                return worn;
            }
        }
        return InventoryHolsterAccess.INSTANCE.findHolster(player, filter);
    }
}
