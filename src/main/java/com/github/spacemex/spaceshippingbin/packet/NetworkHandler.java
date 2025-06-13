package com.github.spacemex.spaceshippingbin.packet;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import static com.github.spacemex.spaceshippingbin.SpaceShippingBin.MODID;

public class NetworkHandler {
    private static final String PROTOCOL = "spaceshippingbin";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(MODID,"main"),
            ()-> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private static int packetId = 0;

    public static void  register(){
        CHANNEL.registerMessage(packetId++,
                SyncCurrencyPacket.class,
                SyncCurrencyPacket::encode,
                SyncCurrencyPacket::decode,
                SyncCurrencyPacket::handle);
    }
}
