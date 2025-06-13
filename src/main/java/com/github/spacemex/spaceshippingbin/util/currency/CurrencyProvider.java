package com.github.spacemex.spaceshippingbin.util.currency;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.github.spacemex.spaceshippingbin.SpaceShippingBin.BALANCE_CAP;

public class CurrencyProvider implements ICapabilitySerializable<CompoundTag> {
    private final Currency currency = new Currency();
    private final LazyOptional<ICurrency> currencyOptional = LazyOptional.of(() -> currency);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == BALANCE_CAP ?  currencyOptional.cast() : LazyOptional.empty();

    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("balance", currency.get());
        tag.putBoolean("initialized", currency.isInitialized());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        currency.set(nbt.getDouble("balance"));
        currency.setInitialized(nbt.getBoolean("initialized"));
    }

    public static ICurrency getCurrency(Player player) {
        return player.getCapability(BALANCE_CAP).orElseThrow(()-> new IllegalStateException("Balance not found on Player!"));
    }
}
