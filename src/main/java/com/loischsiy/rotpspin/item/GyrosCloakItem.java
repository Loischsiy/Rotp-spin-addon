package com.loischsiy.rotpspin.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

/**
 * Gyro's cloak: the addon's own "sail" (SBR ch. 11, the spinning ball holds a cloak open).
 * Worn in the chest slot (instead of a chestplate) or, with Curios, in the back slot.
 * No behaviour of its own: SpinSailHandler finds it through the rotp_spin:spin_sails tag.
 */
public class GyrosCloakItem extends Item {

    public GyrosCloakItem(Properties properties) {
        super(properties);
    }

    // Forge hook: the armor slot accepts it and MobEntity#getEquipmentSlotForItem returns CHEST.
    @Nullable
    @Override
    public EquipmentSlotType getEquipmentSlot(ItemStack stack) {
        return EquipmentSlotType.CHEST;
    }

    // Right click puts it on, like an elytra.
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        EquipmentSlotType slot = MobEntity.getEquipmentSlotForItem(stack);
        if (player.getItemBySlot(slot).isEmpty()) {
            player.setItemSlot(slot, stack.copy());
            stack.setCount(0);
            return ActionResult.sidedSuccess(stack, world.isClientSide());
        }
        return ActionResult.fail(stack);
    }

    /** The cloak sits in the chest slot (the player layer draws it there; Curios skips its own copy). */
    public static boolean isWornOnChest(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlotType.CHEST).getItem() instanceof GyrosCloakItem;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new TranslationTextComponent("item.rotp_spin.gyros_cloak.usage").withStyle(TextFormatting.GRAY));
    }
}
