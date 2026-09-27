package com.loischsiy.rotpspin.network.s2c;

import java.util.function.Supplier;

import com.loischsiy.rotpspin.client.ClientSpinState;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Server -> owning client: current Spin energy for the HUD. Max and throw cost are sent too,
 * because COMMON config values are not synced to clients.
 */
public class SpinEnergySyncPacket {
    private final float energy;
    private final float max;
    private final float spinCost;

    public SpinEnergySyncPacket(float energy, float max, float spinCost) {
        this.energy = energy;
        this.max = max;
        this.spinCost = spinCost;
    }

    public float getEnergy() {
        return energy;
    }

    public float getMax() {
        return max;
    }

    public float getSpinCost() {
        return spinCost;
    }

    public static void encode(SpinEnergySyncPacket msg, PacketBuffer buf) {
        buf.writeFloat(msg.energy);
        buf.writeFloat(msg.max);
        buf.writeFloat(msg.spinCost);
    }

    public static SpinEnergySyncPacket decode(PacketBuffer buf) {
        return new SpinEnergySyncPacket(buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    public static void handle(SpinEnergySyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientSpinState.setEnergy(msg.energy));
        ctx.get().setPacketHandled(true);
    }
}
