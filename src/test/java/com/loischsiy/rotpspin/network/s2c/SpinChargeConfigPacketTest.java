package com.loischsiy.rotpspin.network.s2c;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

class SpinChargeConfigPacketTest {

    @Test
    void encodeDecodeRoundTrip() {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        SpinChargeConfigPacket.encode(new SpinChargeConfigPacket(55, 0.4F), buf);
        SpinChargeConfigPacket decoded = SpinChargeConfigPacket.decode(buf);
        assertEquals(55, decoded.getMaxTicks());
        assertEquals(0.4F, decoded.getChippedMax());
        assertEquals(0, buf.readableBytes());
    }
}
