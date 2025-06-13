package com.github.spacemex.spaceshippingbin.menus;

import com.github.spacemex.spaceshippingbin.SpaceShippingBin;
import com.github.spacemex.spaceshippingbin.shipping.ShippingRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;

public class ShippingBinMenu extends AbstractContainerMenu {
    private final Container container;


    public ShippingBinMenu(int id, Inventory playerInventory, Container container) {
        super(SpaceShippingBin.SHIPPING_BIN_MENU.get(), id);
        this.container = container;

        // Shipping Bin Slots (3 rows x 9 cols)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                int slotIndex = col + row * 9;
                int x = 8 + col * 18;
                int y = 18 + row * 18;

                addSlot(new Slot(container, slotIndex, x, y) {
                    @Override
                    public boolean mayPlace(@NotNull ItemStack stack) {
                        // Only allow valid items with a price
                        return ShippingRegistry.getPrice(stack).isPresent();
                    }
                });
            }
        }

        // Player Inventory (3 rows)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Hotbar (1 row)
        for (int col = 0; col < 9; ++col) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            if (index < container.getContainerSize()) {
                if (!moveItemStackTo(stackInSlot, container.getContainerSize(), slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stackInSlot, 0, container.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }


    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            player.level().playSound(null, player.blockPosition(), SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, 0.75F, 0.75F);
        }
        container.stopOpen(player);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return container.stillValid(player);
    }
}
