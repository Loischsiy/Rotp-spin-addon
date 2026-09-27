package com.loischsiy.rotpspin.network.c2s;

import java.util.function.Supplier;

import com.loischsiy.rotpspin.item.GyrosHolsterItem;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/** Client -> server: the holster throw key was pressed. All checks happen on the server. */
public class HolsterThrowPacket {

    public static void encode(HolsterThrowPacket msg, PacketBuffer buf) {}

    public static HolsterThrowPacket decode(PacketBuffer buf) {
        return new HolsterThrowPacket();
    }

    public static void handle(HolsterThrowPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                GyrosHolsterItem.throwFromHolster(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
