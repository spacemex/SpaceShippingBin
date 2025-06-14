package com.github.spacemex.spaceshippingbin.shipping;

import com.github.spacemex.spaceshippingbin.SpaceShippingBin;
import com.google.gson.JsonParser;

import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

public class ShippingRegistry {
    private static final List<SellableEntry> ENTRIES = new ArrayList<>();

    public static void loadFromFile(Path configDir){
        Path file = configDir.resolve("ShippingBin/SellablesItems.json");
        try {
            if (!Files.exists(file)) {
                Files.createDirectories(file.getParent());
                Files.write(file,List.of("[]"));
            }

            Reader reader = Files.newBufferedReader(file);
            JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();

            ENTRIES.clear();
            for (JsonElement element : array) {
                JsonObject obj = element.getAsJsonObject();
                ResourceLocation item = obj.has("item") ? ResourceLocation.tryParse(obj.get("item").getAsString()) : null;
                ResourceLocation tag = obj.has("tag") ? ResourceLocation.tryParse(obj.get("tag").getAsString()) : null;
                double price = obj.get("price").getAsDouble();
                CompoundTag nbt = null;

                if (obj.has("nbt")){
                    nbt = TagParser.parseTag(obj.get("nbt").toString());
                }

                ENTRIES.add(new SellableEntry(item, tag, nbt, price));
            }
            SpaceShippingBin.LOGGER.info("Shipping Bin: Loaded {} shipping entries.", ENTRIES.size());
            ENTRIES.forEach(entry -> {
                String full = entry.toString();
                int atPos = full.indexOf('@');
                String atAndId = (atPos >= 0)
                        ? full.substring(atPos)
                        : full;

                SpaceShippingBin.LOGGER.info("Added: {}", atAndId);
            });
        }catch (IOException | JsonSyntaxException | CommandSyntaxException e){
            SpaceShippingBin.LOGGER.error("Failed to load SellableItems.json file.", e);
        }
    }

    public static OptionalDouble getPrice(ItemStack stack) {
        for (SellableEntry entry : ENTRIES) {
            if (entry.matches(stack)) {
                return OptionalDouble.of(entry.price());
            }
        }
        return OptionalDouble.empty();
    }
    public static List<SellableEntry> getEntries() {
        return ENTRIES;
    }
}
