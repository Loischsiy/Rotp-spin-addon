package com.loischsiy.rotpspin.compat.curios;

import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.holster.IHolsterAccess;
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

    private CuriosCompat() {}

    // Mod construction: the holster stack gets an ICurio, so Curios draws it in the belt slot.
    public static void init() {
        MinecraftForge.EVENT_BUS.addGenericListener(ItemStack.class, CuriosCompat::attachHolsterCurio);
    }

    private static void attachHolsterCurio(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (stack.getItem() instanceof GyrosHolsterItem) {
            event.addCapability(HOLSTER_CURIO, new HolsterCurio.Provider(stack));
        }
    }

    // InterModEnqueueEvent: make sure the "belt" slot exists; the holster is added to it by the curios:belt item tag.
    public static void enqueueImc() {
        InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE,
                () -> SlotTypePreset.BELT.getMessageBuilder().build());
    }

    public static IHolsterAccess createHolsterAccess() {
        return new CuriosHolsterAccess();
    }
}
