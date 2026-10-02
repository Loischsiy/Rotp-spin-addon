package com.loischsiy.rotpspin.capability;

import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.network.AddonPackets;
import com.loischsiy.rotpspin.network.s2c.SpinChargeConfigPacket;
import com.loischsiy.rotpspin.network.s2c.SpinEnergySyncPacket;
import com.loischsiy.rotpspin.power.SpinPowerType;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/** Registration, attachment and server-side ticking of the Spin energy capability. */
@EventBusSubscriber(modid = AddonMain.MOD_ID)
public class SpinPowerCapability {
    public static final ResourceLocation ID = new ResourceLocation(AddonMain.MOD_ID, "spin_power");

    // Called from FMLCommonSetupEvent (mod bus)
    public static void commonSetupRegister() {
        CapabilityManager.INSTANCE.register(
                SpinPower.class,
                new IStorage<SpinPower>() {
                    @Override public INBT writeNBT(Capability<SpinPower> capability, SpinPower instance, Direction side) { return instance.serializeNBT(); }
                    @Override public void readNBT(Capability<SpinPower> capability, SpinPower instance, Direction side, INBT nbt) { instance.deserializeNBT((CompoundNBT) nbt); }
                },
                SpinPower::new);
    }

    public static LazyOptional<SpinPower> get(Entity entity) {
        return entity.getCapability(SpinPowerProvider.CAPABILITY);
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesEntity(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof PlayerEntity) {
            event.addCapability(ID, new SpinPowerProvider());
        }
    }

    // COMMON config is not synced: hand every client the server's charge settings (sparks of other players too).
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            AddonPackets.sendToClient(new SpinChargeConfigPacket(SpinConfig.CHARGE_MAX_TICKS.get(),
                    SpinConfig.CHARGE_CHIPPED_MAX.get().floatValue()), (ServerPlayerEntity) event.getPlayer());
        }
    }

    // Spin is a learned discipline: keep it after death and on returning from the End.
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        get(event.getOriginal()).ifPresent(oldCap ->
                get(event.getPlayer()).ifPresent(newCap -> newCap.copyFrom(oldCap)));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide()) {
            return;
        }
        float regen = SpinConfig.ENERGY_REGEN_PER_TICK.get().floatValue();
        float max = SpinConfig.ENERGY_MAX.get().floatValue();
        float cost = SpinConfig.BALL_SPIN_COST.get().floatValue();
        boolean hasSpin = SpinPowerType.hasSpin(event.player);
        get(event.player).ifPresent(spin -> {
            // Only a Spin user (RotP non-stand power rotp_spin:spin) regenerates rotation energy.
            if (hasSpin) {
                spin.tick(regen, max);
            }
            if (spin.pollSyncNeeded(max, cost) && event.player instanceof ServerPlayerEntity) {
                AddonPackets.sendToClient(new SpinEnergySyncPacket(spin.getEnergy(), max, cost),
                        (ServerPlayerEntity) event.player);
            }
        });
    }
}
