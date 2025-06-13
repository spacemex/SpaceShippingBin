package com.github.spacemex.spaceshippingbin.util;

import com.github.spacemex.spaceshippingbin.SpaceShippingBin;
import com.github.spacemex.spaceshippingbin.items.AbstractDepositableItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class CoinParser {
    private static final Map<Supplier<Item>, Double> KNOWN_COINS = new HashMap<>();

    public static double getCoinValue(Item item) {
        if (KNOWN_COINS.isEmpty() || item == null) return 0;
        return KNOWN_COINS.entrySet().stream()
                .filter(entry -> entry.getKey().get() == item)
                .mapToDouble(Map.Entry::getValue)
                .findFirst()
                .orElse(0);
    }

    /**
     * Returns a list of ItemStacks whose total value is
     * at least valueToParse, using the largest coins first.
     * If there's a small remainder less than the smallest coin,
     * we round up by adding one smallest coin.
     */
    public static List<ItemStack> createCoinStack(double valueToParse) {
        List<ItemStack> result = new ArrayList<>();
        if (KNOWN_COINS.isEmpty() || valueToParse <= 0) {
            return result;
        }

        // 1. Sort coins by their value descending.
        List<Map.Entry<Supplier<Item>, Double>> coins = new ArrayList<>(KNOWN_COINS.entrySet());
        coins.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        double remaining = valueToParse;
        // 2. Greedily take as many of the highest-value coin as we can.
        for (Map.Entry<Supplier<Item>, Double> entry : coins) {
            double coinValue = entry.getValue();
            if (remaining >= coinValue) {
                int count = (int) (remaining / coinValue);
                if (count > 0) {
                    Item coinItem = entry.getKey().get();
                    result.add(new ItemStack(coinItem, count));
                    remaining -= coinValue * count;
                }
            }
        }

        // 3. If there’s still a small remainder (due to doubles or
        //    no smaller denomination), round up with one smallest coin.
        if (remaining > 1e-6) {
            Map.Entry<Supplier<Item>, Double> smallest = coins.get(coins.size() - 1);
            result.add(new ItemStack(smallest.getKey().get(), 1));
        }

        return result;
    }

    public static void registerCoins(Supplier<Item> ItemSupplier){
        if (KNOWN_COINS.containsKey(ItemSupplier)){
            new Exception("Duplicate Coin Registration: " + ItemSupplier.get().getName(ItemSupplier.get().getDefaultInstance())).printStackTrace();
            return;
        }
        if (ItemSupplier.get() instanceof AbstractDepositableItem) {
            double value = ((AbstractDepositableItem) ItemSupplier.get()).getDepositValue();
            KNOWN_COINS.put(ItemSupplier, value);
            SpaceShippingBin.LOGGER.info("Registered Coin: {} with value: {}", ItemSupplier.get().getName(ItemSupplier.get().getDefaultInstance()), value);
        }else {
            new Exception("Item is not a Coin item: " + ItemSupplier.get().getName(ItemSupplier.get().getDefaultInstance())).printStackTrace();
        }
    }

}
