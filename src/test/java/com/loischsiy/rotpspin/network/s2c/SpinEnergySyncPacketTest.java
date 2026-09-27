package com.loischsiy.rotpspin.network.s2c;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

class SpinEnergySyncPacketTest {

    @Test
    void encodeDecodeRoundTrip() {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        SpinEnergySyncPacket.encode(new SpinEnergySyncPacket(37.5F, 100F, 20F), buf);
        SpinEnergySyncPacket decoded = SpinEnergySyncPacket.decode(buf);
        assertEquals(37.5F, decoded.getEnergy());
        assertEquals(100F, decoded.getMax());
        assertEquals(20F, decoded.getSpinCost());
        assertEquals(0, buf.readableBytes());
    }
}
