package cz.lukesmith.automaticsortermod.block.custom;

import cz.lukesmith.automaticsortermod.AutomaticSorterMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

public class PipeBlock extends Block {

    public static final BooleanProperty NORTH = Properties.NORTH;
    public static final BooleanProperty SOUTH = Properties.SOUTH;
    public static final BooleanProperty WEST = Properties.WEST;
    public static final BooleanProperty EAST = Properties.EAST;
    public static final BooleanProperty UP = Properties.UP;
    public static final BooleanProperty DOWN = Properties.DOWN;

    private static final VoxelShape SHAPE = createCuboidShape(0, 0, 0, 16, 16, 16);

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
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        boolean isConnected = neighborState.getBlock() instanceof PipeBlock ||
                neighborState.getBlock() instanceof FilterBlock ||
                neighborState.getBlock() instanceof SorterControllerBlock;
        return state.with(getPropertyForDirection(direction), isConnected);
    }

    private BooleanProperty getPropertyForDirection(Direction direction) {
        switch (direction) {
            case NORTH: return NORTH;
            case EAST: return EAST;
            case SOUTH: return SOUTH;
            case WEST: return WEST;
            case UP: return UP;
            case DOWN: return DOWN;
            default: throw new IllegalArgumentException("Unexpected direction: " + direction);
        }
    }
}