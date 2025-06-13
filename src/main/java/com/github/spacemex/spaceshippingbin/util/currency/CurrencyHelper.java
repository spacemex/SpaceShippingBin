package com.github.spacemex.spaceshippingbin.util.currency;

import com.github.spacemex.spaceshippingbin.Config;
import com.github.spacemex.spaceshippingbin.packet.NetworkHandler;
import com.github.spacemex.spaceshippingbin.packet.SyncCurrencyPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

import static com.github.spacemex.spaceshippingbin.SpaceShippingBin.BALANCE_CAP;

public class CurrencyHelper {

    private static void sync(Player player, double amount) {
        if (player instanceof ServerPlayer sp){
            NetworkHandler.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> sp),
                    new SyncCurrencyPacket(amount)
            );
        }
    }
    public static void set(Player player, double amount) {
        player.getCapability(BALANCE_CAP).ifPresent(c -> {
            c.set(amount);
            sync(player,c.get());
        });
    }

    public static void add(Player player, double amount) {
        player.getCapability(BALANCE_CAP).ifPresent(c -> {
            c.add(amount);
            sync(player,c.get());
        });
    }

    public static boolean remove(Player player, double amount) {
        return player.getCapability(BALANCE_CAP).map( c -> {
            if (c.hasFunds(amount)) {
                c.remove(amount);
                sync(player,c.get());
                return true;
            } else  {
                return false;
            }
        }).orElse(false);
    }

    public static double get(Player player) {
        return player.getCapability(BALANCE_CAP).map(ICurrency::get).orElse(0.0);
    }

    public static String getFormatted(Player player) {
        return Config.CURRENCY_SYMBOL.get() + CurrencyFormatter.format(get(player));
    }

    public static boolean has(Player player, double amount){
        return player.getCapability(BALANCE_CAP).map(c -> c.hasFunds(amount))
                .orElse(false);
    }
}
