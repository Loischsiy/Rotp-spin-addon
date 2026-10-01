package com.loischsiy.rotpspin.init;

import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.effect.DesiccationEffect;
import com.loischsiy.rotpspin.effect.HemispatialNeglectEffect;
import com.loischsiy.rotpspin.effect.SenescenceEffect;

import net.minecraft.potion.Effect;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class InitEffects {
    // 1.16.5 Forge keeps mob effects in the POTIONS registry (there is no MOB_EFFECTS field).
    public static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS, AddonMain.MOD_ID);

    public static final RegistryObject<HemispatialNeglectEffect> NEGLECT = EFFECTS.register("hemispatial_neglect",
            HemispatialNeglectEffect::new);

    public static final RegistryObject<SenescenceEffect> SENESCENCE = EFFECTS.register("senescence",
            SenescenceEffect::new);

    public static final RegistryObject<DesiccationEffect> DESICCATION = EFFECTS.register("desiccation",
            DesiccationEffect::new);
}
