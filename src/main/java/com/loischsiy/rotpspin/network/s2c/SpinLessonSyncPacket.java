package com.loischsiy.rotpspin.network.s2c;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.loischsiy.rotpspin.client.ClientSpinState;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Server -> owning client: the lesson that is in effect (already {@code MAX} if lessons are disabled
 * in the server config, which is not synced), so RotP's hotbar shows locked techniques correctly.
 * After updating the lesson the HUD controls are rebuilt (as RotP does when Hamon learns a skill),
 * otherwise newly unlocked techniques stay hidden until the world is re-entered.
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
        ctx.get().enqueueWork(() -> {
            ClientSpinState.setLesson(msg.lesson);
            LivingEntity player = ClientUtil.getClientPlayer();
            if (player != null) {
                INonStandPower.getNonStandPowerOptional(player).ifPresent(power -> power.clUpdateHud());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
