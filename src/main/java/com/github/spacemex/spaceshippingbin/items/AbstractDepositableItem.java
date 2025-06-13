package com.github.spacemex.spaceshippingbin.items;

import com.github.spacemex.spaceshippingbin.Config;
import com.github.spacemex.spaceshippingbin.util.currency.CurrencyHelper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractDepositableItem extends Item implements IDepositable {
    public AbstractDepositableItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);

        return deposit(pLevel,pPlayer,pUsedHand,stack) ? InteractionResultHolder.success(stack) : super.use(pLevel,pPlayer,pUsedHand);
    }

    public boolean deposit(Level level, Player player, InteractionHand hand, ItemStack stack) {
        if (Config.ENABLE_VIRTUAL_ECONOMY.get() && player != null && !player.level().isClientSide) {
            int countToUse = player.isShiftKeyDown() ? stack.getCount() : 1;
            double totalValue = getDepositValue() * countToUse;

            CurrencyHelper.add(player, totalValue);

            stack.shrink(countToUse);

            player.level().playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS,
                    level.random.nextFloat(),
                    1.0f
            );

            return true;
        }
        return false;
    }
}
