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
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FilterBlock extends BlockWithEntity implements BlockEntityProvider {

    public static final DirectionProperty FACING = Properties.FACING;

    public FilterBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        // -- Následuje příklad, jak vytvořit hitbox pro blok --
        // Vytvoří se tvar hitboxu bloku
        VoxelShape shape = createCuboidShape(0, 0, 0, 8, 8, 8);

        // Přidá se část hitboxu do bloku
        shape = VoxelShapes.union(shape, createCuboidShape(2, 2, 2, 10, 10, 10));

        // Zde se pak mohou přidávat další části hitboxu
        shape = VoxelShapes.union(shape, createCuboidShape(10, 10, 2, 11, 12, 16));

        // -- Konec příkladu --
        // Můžeš přidávat kolik částí chceš
        // V Minecraftu jsou souřadnice X Y a Z - zapneš je F3
        // funkce createCuboidShape má 6 parametrů - minX, minY, minZ, maxX, maxY, maxZ
        // dohromady tvoří krychli
        // createCuboidShape(0, 0, 0, 8, 8, 8) - vytvoří krychli od 0,0,0 do 8,8,8
        // createCuboidShape(2, 2, 2, 10, 10, 10) - vytvoří krychli od 2,2,2 do 10,10,10
        // minimální hodnota je 0 a maximální 16
        // musíš mít nejprve vytvořený tvar a potom k němu přidávat další části (viz. kód výše)

        // Požaduji alespoň dvě části hitboxu - jednu částo pro iron část bloku a druhou pro pipe část bloku
        // Kdyžtak vyzkoušej, jak se po tom bude chodit, ve světě jsou už položené bloky na zkoušku

        // Na konci funkce musíš vrátit tvar hitboxu (kód níže)
        return shape;
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
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
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