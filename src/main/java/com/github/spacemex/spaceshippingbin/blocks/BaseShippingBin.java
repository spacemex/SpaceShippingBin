package com.github.spacemex.spaceshippingbin.blocks;

import com.github.spacemex.spaceshippingbin.SpaceShippingBin;
import com.github.spacemex.spaceshippingbin.entities.block.BaseShippingBinEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BaseShippingBin extends BaseEntityBlock {
    public BaseShippingBin() {
        super(Properties.copy(Blocks.BARREL).strength(2.5f).noOcclusion());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return new BaseShippingBinEntity(pPos,pState);
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull InteractionResult use(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer,
                                          @NotNull InteractionHand pHand, @NotNull BlockHitResult pHit) {
        if (!pLevel.isClientSide){
            BlockEntity be = pLevel.getBlockEntity(pPos);
            if (be instanceof BaseShippingBinEntity bin){
                NetworkHooks.openScreen((ServerPlayer) pPlayer,(MenuProvider) bin,pPos);

                pLevel.playSound(null,pPos, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS,1.0f,1.0f);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level pLevel, @NotNull BlockState pState,
                                                                            @NotNull BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide) return null;
        if (pBlockEntityType == SpaceShippingBin.SHIPPING_BIN_ENTITY.get()) {
            return (level1, pos, state1, blockEntity) -> {
                if (blockEntity instanceof BaseShippingBinEntity shippingBin) {
                    BaseShippingBinEntity.tick(level1, pos, state1, shippingBin);
                }
            };
        }
        return null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasAnalogOutputSignal(@NotNull BlockState pState) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getAnalogOutputSignal(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos) {
        return super.getAnalogOutputSignal(pState, pLevel, pPos);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return true;
    }
}
