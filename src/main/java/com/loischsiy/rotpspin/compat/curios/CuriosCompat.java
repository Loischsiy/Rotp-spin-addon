package com.loischsiy.rotpspin.compat.curios;

import net.minecraft.entity.player.PlayerEntity;

import java.util.function.Predicate;

import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.holster.IHolsterAccess;
import com.loischsiy.rotpspin.item.GyrosCloakItem;
import com.loischsiy.rotpspin.item.GyrosHolsterItem;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.InterModComms;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;

/**
 * The only entry point into Curios classes. Must be referenced only after
 * {@code ModList.get().isLoaded("curios")} is confirmed (docs/integrations.md).
 */
public final class CuriosCompat {
    public static final String MOD_ID = "curios";

    private static final ResourceLocation HOLSTER_CURIO = new ResourceLocation(AddonMain.MOD_ID, "holster_curio");
    private static final ResourceLocation CLOAK_CURIO = new ResourceLocation(AddonMain.MOD_ID, "cloak_curio");

    private CuriosCompat() {}

    // Mod construction: holster and cloak stacks get an ICurio, so Curios draws them in the belt and back slots.
    public static void init() {
        MinecraftForge.EVENT_BUS.addGenericListener(ItemStack.class, CuriosCompat::attachHolsterCurio);
    }

    private static void attachHolsterCurio(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (stack.getItem() instanceof GyrosHolsterItem) {
            event.addCapability(HOLSTER_CURIO, new HolsterCurio.Provider(stack));
        } else if (stack.getItem() instanceof GyrosCloakItem) {
            event.addCapability(CLOAK_CURIO, new CloakCurio.Provider());
        }
    }

    // InterModEnqueueEvent: make sure the "belt" and "back" slots exist; the holster and the cloak are added to them
    // by the curios:belt and curios:back item tags.
    public static void enqueueImc() {
        InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE,
                () -> SlotTypePreset.BELT.getMessageBuilder().build());
        InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE,
                () -> SlotTypePreset.BACK.getMessageBuilder().build());
    }

    /** A matching item worn in any Curios slot (the spin sail looks for a cloak from other mods). */
    public static boolean hasCurio(PlayerEntity player, Predicate<ItemStack> filter) {
        return CuriosApi.getCuriosHelper().findFirstCurio(player, filter).isPresent();
    }

    public static IHolsterAccess createHolsterAccess() {
        return new CuriosHolsterAccess();
    }
}
