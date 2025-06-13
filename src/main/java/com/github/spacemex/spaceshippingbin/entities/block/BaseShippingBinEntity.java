package com.github.spacemex.spaceshippingbin.entities.block;

import com.github.spacemex.spaceshippingbin.Config;
import com.github.spacemex.spaceshippingbin.SpaceShippingBin;
import com.github.spacemex.spaceshippingbin.menus.ShippingBinMenu;
import com.github.spacemex.spaceshippingbin.shipping.ShippingRegistry;
import com.github.spacemex.spaceshippingbin.util.CoinParser;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.OptionalDouble;


public class BaseShippingBinEntity extends RandomizableContainerBlockEntity implements MenuProvider, Clearable, WorldlyContainer {
    private final LazyOptional<IItemHandlerModifiable>[] handlers = SidedInvWrapper.create(this, Direction.values());

    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private static final int SLOTS = 27;
    private boolean wasPowered = false;
    private double totalSellValue = 0.0;

    public BaseShippingBinEntity(BlockPos pos, BlockState state) {
        super(SpaceShippingBin.SHIPPING_BIN_ENTITY.get(), pos,state);
    }

    @Override
    public @NotNull NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    public void setItems(@NotNull NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public boolean canOpen(@NotNull Player pPlayer) {
        if (level == null) return false;

        BlockPos pos = getBlockPos();
        return level.getBlockState(pos.above()).isAir();
    }

    @Override
    public @NotNull Component getDefaultName() {
        return Component.literal("Shipping Bin");
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pInventory) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBlockPos(getBlockPos());
        return new ShippingBinMenu(pContainerId, pInventory, this);    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (!this.remove && cap == ForgeCapabilities.ITEM_HANDLER && side != null) {
            return LazyOptional.of(() -> new SidedInvWrapper(this, side)).cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public int getContainerSize() {
        return SLOTS;
    }

    public void sellContents(){
        NonNullList<ItemStack> items = getItems();
        for (int i = 0; i < items.size(); i++){
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()){
                OptionalDouble price = ShippingRegistry.getPrice(stack);
                int finalI = i;

                price.ifPresent(v -> {
                    totalSellValue += v * stack.getCount();
                    items.set(finalI, ItemStack.EMPTY);
                });
            }
        }
        if (totalSellValue <= 0) return;

        if (Config.ENABLE_VIRTUAL_ECONOMY.get()) {
            // if checks enabled
            ItemStack checkItem = new ItemStack(SpaceShippingBin.CHECK.get());
            CompoundTag tag = checkItem.getOrCreateTag();
            tag.putDouble("check_value", totalSellValue);
            checkItem.setTag(tag);

            boolean inserted = false;
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).isEmpty()) {
                    items.set(i, checkItem);
                    inserted = true;
                    break;
                }
            }

            if (!inserted && level != null) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), checkItem);
            }

            totalSellValue = 0.0;
            setChanged();
            return;
        }

        // 4) Coin payout: split & insert each coin stack
        List<ItemStack> coinStacks = CoinParser.createCoinStack(totalSellValue);

        for (ItemStack fullStack : coinStacks) {
            Item coinItem = fullStack.getItem();
            int toInsert = fullStack.getCount();
            int maxPerSlot = fullStack.getMaxStackSize();

            // 4a) First, try merging into existing partial stacks
            for (int i = 0; i < items.size() && toInsert > 0; i++) {
                ItemStack slot = items.get(i);
                if (!slot.isEmpty()
                        && slot.getItem() == coinItem
                        && slot.getCount() < maxPerSlot)
                {
                    int space = maxPerSlot - slot.getCount();
                    int moved = Math.min(space, toInsert);
                    slot.grow(moved);
                    toInsert -= moved;
                }
            }

            // 4b) Then, fill empty slots in chunks of maxPerSlot
            while (toInsert > 0) {
                int chunk = Math.min(maxPerSlot, toInsert);
                ItemStack chunkStack = new ItemStack(coinItem, chunk);

                boolean placed = false;
                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i).isEmpty()) {
                        items.set(i, chunkStack);
                        placed = true;
                        break;
                    }
                }

                if (!placed && level != null) {
                    // no room left: drop the rest
                    Containers.dropItemStack(
                            level,
                            worldPosition.getX(),
                            worldPosition.getY(),
                            worldPosition.getZ(),
                            chunkStack
                    );
                }

                toInsert -= chunk;
            }
        }

        // 5) Reset & mark dirty
        totalSellValue = 0.0;
        setChanged();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        super.saveAdditional(pTag);
        ContainerHelper.saveAllItems(pTag,this.items);
        pTag.putBoolean("was_powered", this.wasPowered);
        pTag.putDouble("total_value", this.totalSellValue);
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(pTag,this.items);

        if (pTag.contains("was_powered")) {
            wasPowered = pTag.getBoolean("was_powered");
        }
        if (pTag.contains("total_value")) {
            totalSellValue = pTag.getDouble("total_value");
        }
    }

    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction side) {
        int[] slots = new int[getContainerSize()];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = i;
        }
        return slots;
    }

    @Override
    public boolean canPlaceItemThroughFace(int pIndex, @NotNull ItemStack pItemStack, @Nullable Direction pDirection) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int pIndex, @NotNull ItemStack pStack, @NotNull Direction pDirection) {
        return ShippingRegistry.getPrice(pStack).isEmpty();
    }

    @Override
    public @NotNull ItemStack getItem(int pIndex) {
        return items.get(pIndex);
    }

    @Override
    public void setItem(int pIndex, @NotNull ItemStack pStack) {
        items.set(pIndex, pStack);
        setChanged();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemStack : items) {
            if (!itemStack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull ItemStack removeItem(int pIndex, int pCount) {
        ItemStack result = ContainerHelper.removeItem(this.items, pIndex, pCount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int pIndex) {
        ItemStack result = ContainerHelper.takeItem(this.items, pIndex);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public void clearContent() {
        this.items.clear();
        setChanged();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        for (LazyOptional<IItemHandlerModifiable> handler : handlers) {
            handler.invalidate();
        }
    }

    @SuppressWarnings("unused")
    public static void tick(Level level, BlockPos pos, BlockState state, BaseShippingBinEntity entity) {
        boolean isPowered = level.hasNeighborSignal(pos);
        if (isPowered != entity.wasPowered){
            entity.sellContents();
        }
        entity.wasPowered = isPowered;
    }
}
