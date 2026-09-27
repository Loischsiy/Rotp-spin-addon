package com.loischsiy.rotpspin.network;

import java.util.Optional;

import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.network.c2s.HolsterThrowPacket;
import com.loischsiy.rotpspin.network.s2c.SpinEnergySyncPacket;
import com.loischsiy.rotpspin.network.s2c.SpinLessonSyncPacket;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

/** Own network channel of the addon (pattern: RotP-Addon-example, branch player-capability). */
public class AddonPackets {
    private static final String PROTOCOL_VERSION = "1";
    private static SimpleChannel channel;

    // Called from FMLCommonSetupEvent
    public static void init() {
        channel = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(AddonMain.MOD_ID, "channel"))
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .simpleChannel();

        int packetIndex = 0;
        channel.registerMessage(packetIndex++, SpinEnergySyncPacket.class,
                SpinEnergySyncPacket::encode, SpinEnergySyncPacket::decode, SpinEnergySyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(packetIndex++, HolsterThrowPacket.class,
                HolsterThrowPacket::encode, HolsterThrowPacket::decode, HolsterThrowPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(packetIndex++, SpinLessonSyncPacket.class,
                SpinLessonSyncPacket::encode, SpinLessonSyncPacket::decode, SpinLessonSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void sendToServer(Object msg) {
        channel.sendToServer(msg);
    }

    public static void sendToClient(Object msg, ServerPlayerEntity player) {
        if (!(player instanceof FakePlayer)) {
            channel.send(PacketDistributor.PLAYER.with(() -> player), msg);
        }
    }
}
