package com.loischsiy.rotpspin.init;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.loischsiy.rotpspin.AddonMain;

import net.minecraftforge.registries.DeferredRegister;

public class InitStands {
    @SuppressWarnings("unchecked")
    public static final DeferredRegister<Action<?>> ACTIONS = DeferredRegister.create(
            (Class<Action<?>>) ((Class<?>) Action.class), AddonMain.MOD_ID);
    @SuppressWarnings("unchecked")
    public static final DeferredRegister<StandType<?>> STANDS = DeferredRegister.create(
            (Class<StandType<?>>) ((Class<?>) StandType.class), AddonMain.MOD_ID);
}
