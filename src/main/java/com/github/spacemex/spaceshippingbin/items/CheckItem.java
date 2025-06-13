package com.github.spacemex.spaceshippingbin.items;

import com.github.spacemex.spaceshippingbin.Config;
import com.github.spacemex.spaceshippingbin.util.currency.CurrencyFormatter;
import com.github.spacemex.spaceshippingbin.util.currency.CurrencyHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class CheckItem extends AbstractDepositableItem{

    private  double amount;
    private static final String NBT = "check_value";
    public CheckItem(Properties pProperties) {
        super(pProperties.stacksTo(1));
    }


    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);
        return deposit(pLevel,pPlayer,pUsedHand,stack) ? InteractionResultHolder.success(stack) : super.use(pLevel,pPlayer,pUsedHand);
    }

    @Override
    public boolean deposit(Level level, Player player, InteractionHand hand, ItemStack stack) {
        if (!level.isClientSide && stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if (Objects.requireNonNull(tag).contains(NBT)) {
                 amount = tag.getDouble(NBT);

                CurrencyHelper.add(player, amount);
                stack.shrink(1);

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
        }
        return false;
    }

    @Override
    public double getDepositValue() {
        return amount;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, @Nullable Level pLevel, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);

        if (pStack.hasTag()){
            CompoundTag tag = pStack.getTag();
            if (Objects.requireNonNull(tag).contains(NBT)){
                double value = tag.getDouble(NBT);

                pTooltipComponents.add(
                        Component.literal(Config.CURRENCY_SYMBOL.get() + CurrencyFormatter.format(value)).withStyle(ChatFormatting.GREEN)
                );
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return pStack.hasTag() && Objects.requireNonNull(pStack.getTag()).contains(NBT);
    }
}
