package org.teacon.xkdeco.block;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.teacon.xkdeco.blockentity.ItemDisplayBlockEntity;
import org.teacon.xkdeco.util.MathUtil;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.teacon.xkdeco.util.MathUtil.TAU;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class SpecialItemDisplayBlock extends BaseEntityBlock implements XKDecoBlock.Special {
    private static final VoxelShape TOP = Block.box(0, 13, 0, 16, 16, 16);
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(3, 0, 3, 13, 4, 13),
            Block.box(5, 4, 5, 11, 11, 11),
            Block.box(1, 11, 1, 15, 13, 15),
            TOP
    );
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public SpecialItemDisplayBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(POWERED, false));
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
        return RenderShape.MODEL;
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return new ItemDisplayBlockEntity(pPos, pState);
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level worldIn, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand handIn, BlockHitResult hit) {
        // must click on the upper surface
        if (hit.getDirection() != Direction.UP
                || !MathUtil.containsInclusive(TOP.bounds(), hit.getLocation().subtract(Vec3.atLowerCornerOf(pos)))) {
            return InteractionResult.PASS;
        }

        if (!worldIn.isClientSide()) {
            var te = worldIn.getBlockEntity(pos);
            if (te instanceof ItemDisplayBlockEntity tileEntity) {
                var temp = player.getItemInHand(handIn).copy();
                player.setItemInHand(handIn, tileEntity.getItem().copy());
                tileEntity.setItem(temp);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
        return (level, pos, blockState, blockEntity) -> {
            if (blockEntity instanceof ItemDisplayBlockEntity itemDisplayBlockEntity) {
                var spin = itemDisplayBlockEntity.getSpin();
                if (itemDisplayBlockEntity.getBlockState().getValue(SpecialItemDisplayBlock.POWERED)) {
                    spin = (float) (Math.round(spin / (TAU / 8)) * (TAU / 8));
                } else {
                    spin += 0.05f;
                    if (spin >= TAU) {
                        spin -= TAU;
                    }
                }
                itemDisplayBlockEntity.setSpin(spin);
            }
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(POWERED, pContext.getLevel().hasNeighborSignal(pContext.getClickedPos()));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(@NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos, @NotNull Block pBlock, @NotNull BlockPos pFromPos, boolean pIsMoving) {
        if (!pLevel.isClientSide) {
            if (pState.getValue(POWERED) != pLevel.hasNeighborSignal(pPos)) {
                pLevel.setBlock(pPos, pState.cycle(POWERED), 2);
            }
        }
    }
}