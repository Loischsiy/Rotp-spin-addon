package com.loischsiy.rotpspin.network.s2c;

import java.util.function.Supplier;

import com.loischsiy.rotpspin.client.ClientSpinState;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Server -> owning client: the lesson that is in effect (already {@code MAX} if lessons are disabled
 * in the server config, which is not synced), so RotP's hotbar shows locked techniques correctly.
 */
public class SpinLessonSyncPacket {
    private final int lesson;

    public SpinLessonSyncPacket(int lesson) {
        this.lesson = lesson;
    }

    public int getLesson() {
        return lesson;
    }

    public static void encode(SpinLessonSyncPacket msg, PacketBuffer buf) {
        buf.writeVarInt(msg.lesson);
    }

    public static SpinLessonSyncPacket decode(PacketBuffer buf) {
        return new SpinLessonSyncPacket(buf.readVarInt());
    }

    public static void handle(SpinLessonSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientSpinState.setLesson(msg.lesson));
        ctx.get().setPacketHandled(true);
    }
}
