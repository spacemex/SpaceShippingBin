package com.github.spacemex.spaceshippingbin.shipping;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public record SellableEntry(ResourceLocation itemId, ResourceLocation tagId, CompoundTag matchNbt, double price) {

    public boolean matches(ItemStack stack) {
        if (itemId != null) {
            ResourceLocation actualId = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (!itemId.equals(actualId)) return false;
        }

        if (tagId != null) {
            TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagId);
            if (!stack.is(tagKey)) return false;
        }

        if (matchNbt != null) {
            if (!stack.hasTag() || !tagContains(stack.getTag(), matchNbt)) return false;
        }

        return true;
    }

    private boolean tagContains(CompoundTag actual, CompoundTag expected) {
        for (String key : expected.getAllKeys()) {
            if (!actual.contains(key)) return false;

            if (expected.get(key) instanceof CompoundTag expectedCompound &&
                    actual.get(key) instanceof CompoundTag actualCompound) {
                if (!tagContains(actualCompound, expectedCompound)) return false;
            } else if (!expected.get(key).equals(actual.get(key))) {
                return false;
            }
        }
        return true;
    }
}
