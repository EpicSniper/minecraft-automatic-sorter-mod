package cz.lukesmith.automaticsortermod.block.entity;

import cz.lukesmith.automaticsortermod.AutomaticSorterMod;
import cz.lukesmith.automaticsortermod.block.custom.FilterBlock;
import cz.lukesmith.automaticsortermod.block.custom.PipeBlock;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class SorterControllerBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory {

    public SorterControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SORTER_CONTROLLER_BLOCK_ENTITY, pos, state);
    }

    public static SorterControllerBlockEntity create(BlockPos pos, BlockState state) {
        return new SorterControllerBlockEntity(pos, state);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return DefaultedList.ofSize(1, ItemStack.EMPTY);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf packetByteBuf) {

    }

    @Override
    public Text getDisplayName() {
        return null;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return null;
    }

    public static void tick(World world, BlockPos pos, BlockState state) {
        if (world.isClient) {
            return;
        }

        Set<BlockPos> connectedPipes = findConnectedPipes(world, pos);
        Set<BlockPos> connectedFilters = findConnectedFilters(world, connectedPipes);
        AutomaticSorterMod.LOGGER.info("Connected pipes: " + connectedFilters);
    }

    private static Set<BlockPos> findConnectedPipes(World world, BlockPos startPos) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(startPos);

        while (!queue.isEmpty()) {
            BlockPos currentPos = queue.poll();
            if (!visited.contains(currentPos)) {
                visited.add(currentPos);

                for (Direction direction : Direction.values()) {
                    BlockPos neighborPos = currentPos.offset(direction);
                    if (world.getBlockState(neighborPos).getBlock() instanceof PipeBlock) {
                        queue.add(neighborPos);
                    }
                }
            }
        }

        visited.remove(startPos); // Remove the starting position (SorterControllerBlock)
        return visited;
    }

    private static Set<BlockPos> findConnectedFilters(World world, Set<BlockPos> pipePositions) {
        Set<BlockPos> filterPositions = new HashSet<>();

        for (BlockPos pipePos : pipePositions) {
            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = pipePos.offset(direction);
                if (world.getBlockState(neighborPos).getBlock() instanceof FilterBlock) {
                    filterPositions.add(neighborPos);
                }
            }
        }

        return filterPositions;
    }
}
