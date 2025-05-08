package cz.lukesmith.automaticsorter.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class PipeBlock extends Block {

    public static final BooleanProperty NORTH = Properties.NORTH;
    public static final BooleanProperty SOUTH = Properties.SOUTH;
    public static final BooleanProperty WEST = Properties.WEST;
    public static final BooleanProperty EAST = Properties.EAST;
    public static final BooleanProperty UP = Properties.UP;
    public static final BooleanProperty DOWN = Properties.DOWN;

    public PipeBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(NORTH, false)
                .with(EAST, false)
                .with(SOUTH, false)
                .with(WEST, false)
                .with(UP, false)
                .with(DOWN, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape shape = createCuboidShape(6, 6, 6, 10, 10, 10);

        int[][] directions = {
                {6, 6, 0, 10, 10, 6},
                {6, 6, 10, 10, 10, 16},
                {10, 6, 6, 16, 10, 10},
                {0, 6, 6, 6, 10, 10},
                {6, 10, 6, 10, 16, 10},
                {6, 0, 6, 10, 6, 10}
        };

        boolean[] directionsActive = {
                state.get(NORTH),
                state.get(SOUTH),
                state.get(EAST),
                state.get(WEST),
                state.get(UP),
                state.get(DOWN)
        };

        for (int i = 0; i < directions.length; i++) {
            if (directionsActive[i]) {
                shape = VoxelShapes.union(shape, createCuboidShape(directions[i][0], directions[i][1], directions[i][2], directions[i][3], directions[i][4], directions[i][5]));
            }
        }

        return shape;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        boolean isConnected = isConnectedToNeighbor(neighborState, direction);
        return state.with(getPropertyForDirection(direction), isConnected);
    }

    private boolean isConnectedToNeighbor(BlockState neighborState, Direction direction) {
        if (neighborState.getBlock() instanceof SorterControllerBlock || neighborState.getBlock() instanceof FilterBlock) {
            Direction filterFacing = neighborState.get(FilterBlock.FACING);
            return direction == filterFacing;
        } else return neighborState.getBlock() instanceof PipeBlock;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.offset(direction);
            BlockState neighborState = world.getBlockState(neighborPos);
            state = state.with(getPropertyForDirection(direction), isConnectedToNeighbor(neighborState, direction));
        }

        world.setBlockState(pos, state, 3);
    }

    private BooleanProperty getPropertyForDirection(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }
}