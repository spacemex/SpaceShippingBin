package com.github.spacemex.spaceshippingbin.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncCurrencyPacket(double currency) {
    public static void encode(SyncCurrencyPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.currency);
    }

    public static SyncCurrencyPacket decode(FriendlyByteBuf buf) {
        return new SyncCurrencyPacket(buf.readDouble());
    }

    public static void handle(SyncCurrencyPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                ClientCurrencyData.setBalance(msg.currency);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
