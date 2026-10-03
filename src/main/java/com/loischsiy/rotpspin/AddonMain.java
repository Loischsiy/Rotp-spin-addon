package com.loischsiy.rotpspin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.loischsiy.rotpspin.capability.SpinPowerCapability;
import com.loischsiy.rotpspin.power.SpinSailHandler;
import com.loischsiy.rotpspin.compat.curios.CuriosCompat;
import com.loischsiy.rotpspin.compat.d4c.LoveTrainBypass;
import com.loischsiy.rotpspin.compat.tusk.ITuskCompat;
import com.loischsiy.rotpspin.compat.tusk.TuskCompat;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.entity.GyroTeacherEntity;
import com.loischsiy.rotpspin.holster.HolsterAccess;
import com.loischsiy.rotpspin.init.InitEffects;
import com.loischsiy.rotpspin.init.InitEntities;
import com.loischsiy.rotpspin.init.InitItems;
import com.loischsiy.rotpspin.init.InitPowers;
import com.loischsiy.rotpspin.init.InitStands;
import com.loischsiy.rotpspin.network.AddonPackets;
import com.loischsiy.rotpspin.world.GyroSpawns;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AddonMain.MOD_ID)
public class AddonMain {
    // Must match the "modId" entry in META-INF/mods.toml
    public static final String MOD_ID = "rotp_spin";
    public static final Logger LOGGER = LogManager.getLogger();
    // Literal on purpose: the core must not touch compat classes before the mod is confirmed.
    private static final String CURIOS_MOD_ID = "curios";
    private static final String TUSK_MOD_ID = "rotp_t";
    private static final String D4C_MOD_ID = "rotp_d4c";
    private static ITuskCompat tuskCompat = ITuskCompat.NOOP;

    public AddonMain() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SpinConfig.SPEC);

        InitItems.ITEMS.register(modEventBus);
        InitEntities.ENTITIES.register(modEventBus);
        InitEffects.EFFECTS.register(modEventBus);
        InitStands.ACTIONS.register(modEventBus);
        InitStands.STANDS.register(modEventBus);
        InitPowers.NON_STAND_POWERS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::enqueueImc);
        modEventBus.addListener(this::entityAttributes);
        if (isCuriosLoaded()) {
            CuriosCompat.init();
        }
        if (ModList.get().isLoaded(D4C_MOD_ID)) {
            LoveTrainBypass.markLoaded();
            MinecraftForge.EVENT_BUS.register(LoveTrainBypass.class);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        AddonPackets.init();
        SpinPowerCapability.commonSetupRegister();
        event.enqueueWork(GyroSpawns::registerPlacement);
        // Optional integrations: compat classes are loaded only when the mod is present (docs/integrations.md).
        if (isCuriosLoaded()) {
            HolsterAccess.set(CuriosCompat.createHolsterAccess());
            SpinSailHandler.setCurioFinder(CuriosCompat::hasCurio);
        }
        if (isTuskLoaded()) {
            tuskCompat = new TuskCompat();
        }
    }

    private void entityAttributes(EntityAttributeCreationEvent event) {
        event.put(InitEntities.GYRO_TEACHER.get(), GyroTeacherEntity.createAttributes().build());
    }

    private void enqueueImc(InterModEnqueueEvent event) {
        if (isCuriosLoaded()) {
            CuriosCompat.enqueueImc();
        }
    }

    private static boolean isCuriosLoaded() {
        return ModList.get().isLoaded(CURIOS_MOD_ID);
    }

    public static ITuskCompat getTuskCompat() {
        return tuskCompat;
    }

    private static boolean isTuskLoaded() {
        return ModList.get().isLoaded(TUSK_MOD_ID);
    }
}
