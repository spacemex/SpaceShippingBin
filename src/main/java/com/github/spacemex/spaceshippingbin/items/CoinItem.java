package com.github.spacemex.spaceshippingbin.items;

import com.github.spacemex.spaceshippingbin.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CoinItem extends AbstractDepositableItem{
    private final double value;
    public CoinItem(Properties pProperties, double value) {
        super(pProperties);
        this.value = value;
    }

    @Override
    public double getDepositValue() {
        return value;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, @Nullable Level pLevel, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);

        if (value > 0){
            pTooltipComponents.add(Component.literal(Config.CURRENCY_SYMBOL.get() + value).withStyle());
        }
    }
}
