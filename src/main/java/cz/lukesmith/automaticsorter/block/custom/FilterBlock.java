package cz.lukesmith.automaticsorter.block.custom;

import com.mojang.serialization.MapCodec;
import cz.lukesmith.automaticsorter.block.entity.FilterBlockEntity;
import cz.lukesmith.automaticsorter.block.entity.ModBlockEntities;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FilterBlock extends BlockWithEntity implements BlockEntityProvider {

    public static final DirectionProperty FACING = Properties.FACING;

    public FilterBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.UP));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Vec3d[][] shapes = getShapesForFacing(state.get(FACING));

        VoxelShape shape = VoxelShapes.empty();
        for (Vec3d[] shapePart : shapes) {
            shapePart[0] = shapePart[0].multiply(1 / 16.0);
            shapePart[1] = shapePart[1].multiply(1 / 16.0);
            shape = VoxelShapes.union(shape, VoxelShapes.cuboid(shapePart[0].getX(), shapePart[0].getY(), shapePart[0].getZ(), shapePart[1].getX(), shapePart[1].getY(), shapePart[1].getZ()));
        }

        return shape;
    }

    private Vec3d[][] getShapesForFacing(Direction facing) {
        Vec3d[][] shapes = new Vec3d[][]{
                {new Vec3d(6, 0, 6), new Vec3d(10, 9, 10)},
                {new Vec3d(3, 9, 3), new Vec3d(13, 13, 13)},
                {new Vec3d(2, 13, 2), new Vec3d(14, 16, 14)}
        };

        switch (facing) {
            case DOWN:
                shapes[0] = new Vec3d[]{new Vec3d(6, 7, 6), new Vec3d(10, 16, 10)};
                shapes[1] = new Vec3d[]{new Vec3d(3, 3, 3), new Vec3d(13, 7, 13)};
                shapes[2] = new Vec3d[]{new Vec3d(2, 0, 2), new Vec3d(14, 3, 14)};
                break;
            case NORTH:
                shapes[0] = new Vec3d[]{new Vec3d(6, 6, 7), new Vec3d(10, 10, 16)};
                shapes[1] = new Vec3d[]{new Vec3d(3, 3, 3), new Vec3d(13, 13, 7)};
                shapes[2] = new Vec3d[]{new Vec3d(2, 2, 0), new Vec3d(14, 14, 3)};
                break;
            case SOUTH:
                shapes[0] = new Vec3d[]{new Vec3d(6, 6, 0), new Vec3d(10, 10, 9)};
                shapes[1] = new Vec3d[]{new Vec3d(3, 3, 9), new Vec3d(13, 13, 13)};
                shapes[2] = new Vec3d[]{new Vec3d(2, 2, 13), new Vec3d(14, 14, 16)};
                break;
            case WEST:
                shapes[0] = new Vec3d[]{new Vec3d(7, 6, 6), new Vec3d(16, 10, 10)};
                shapes[1] = new Vec3d[]{new Vec3d(3, 3, 3), new Vec3d(7, 13, 13)};
                shapes[2] = new Vec3d[]{new Vec3d(0, 2, 2), new Vec3d(3, 14, 14)};
                break;
            case EAST:
                shapes[0] = new Vec3d[]{new Vec3d(0, 6, 6), new Vec3d(9, 10, 10)};
                shapes[1] = new Vec3d[]{new Vec3d(9, 3, 3), new Vec3d(13, 13, 13)};
                shapes[2] = new Vec3d[]{new Vec3d(13, 2, 2), new Vec3d(16, 14, 14)};
                break;
        }

        return shapes;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new FilterBlockEntity(pos, state);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof FilterBlockEntity) {
                ItemScatterer.spawn(world, pos, (FilterBlockEntity) blockEntity);
                world.updateComparators(pos, this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            NamedScreenHandlerFactory screenHandlerFactory = ((FilterBlockEntity) world.getBlockEntity(pos));

            if (screenHandlerFactory != null) {
                player.openHandledScreen(screenHandlerFactory);
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, ModBlockEntities.FILTER_BLOCK_ENTITY,
                (world1, pos, state1, blockEntity) -> blockEntity.tick(world1, pos, state1));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getSide().getOpposite());
    }
}