package com.loischsiy.rotpspin.network.s2c;

import java.util.function.Supplier;

import com.loischsiy.rotpspin.client.ClientSpinState;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Server -> every client on login: the charge settings of "Spin Charge". COMMON config is not synced,
 * and the charge sparks are drawn for every player watching the user, so each client needs the
 * server values to show the charge exactly.
 */
public class SpinChargeConfigPacket {
    private final int maxTicks;
    private final float chippedMax;

    public SpinChargeConfigPacket(int maxTicks, float chippedMax) {
        this.maxTicks = maxTicks;
        this.chippedMax = chippedMax;
    }

    public int getMaxTicks() {
        return maxTicks;
    }

    public float getChippedMax() {
        return chippedMax;
    }

    public static void encode(SpinChargeConfigPacket msg, PacketBuffer buf) {
        buf.writeVarInt(msg.maxTicks);
        buf.writeFloat(msg.chippedMax);
    }

    public static SpinChargeConfigPacket decode(PacketBuffer buf) {
        return new SpinChargeConfigPacket(buf.readVarInt(), buf.readFloat());
    }

    public static void handle(SpinChargeConfigPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientSpinState.setChargeConfig(msg.maxTicks, msg.chippedMax));
        ctx.get().setPacketHandled(true);
    }
}
