package com.loischsiy.rotpspin.holster;

import java.util.function.Predicate;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * Where the player's holster is worn. Throw logic depends only on this interface, so an optional
 * Curios "belt" slot can be added later as another implementation (see docs/rotp-playbook.md, D).
 */
public interface IHolsterAccess {

    /** First holster matching {@code filter}, or {@link ItemStack#EMPTY}. The stack is live: changes are saved. */
    ItemStack findHolster(PlayerEntity player, Predicate<ItemStack> filter);

    static IHolsterAccess current() {
        return HolsterAccess.get();
    }
}
